package com.szabh.androiddfu.goodix;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodix.ble.gr.libdfu.EasyDfu;
import com.goodix.ble.libcomx.transceiver.IFrameSender;
import com.szabh.androiddfu.goodix.ble_connect.BLEConnectCallback;
import com.szabh.androiddfu.goodix.ble_connect.BLEConnectManager;

import java.util.UUID;

public class DfuConnection {
    private static final String TAG = "GoodixDfu";

    private final static UUID DFU_SERVICE_UUID = UUID.fromString("a6ed0401-d344-460a-8075-b9e8ec90d71b");
    private final static UUID DFU_RECEIVE_CHARACTERISTIC_UUID = UUID.fromString("a6ed0402-d344-460a-8075-b9e8ec90d71b");
    private final static UUID DFU_WRITE_CHARACTERISTIC_UUID = UUID.fromString("a6ed0403-d344-460a-8075-b9e8ec90d71b");
    private final static UUID DFU_CONTROL_CHARACTERISTIC_UUID = UUID.fromString("a6ed0404-d344-460a-8075-b9e8ec90d71b");
    private final static int MTU_SEND_SIZE = 247;

    private BluetoothGattCharacteristic mDfuReceiveCharacteristic, mDfuWriteCharacteristic;

    private BluetoothDevice device;
    private BLEConnectManager connection;
    private EasyDfu dfu = new EasyDfu();
    private Context mCtx;
    private MutableLiveData<ConnState> connectionState = new MutableLiveData<>();
    private MutableLiveData<String> errorInfo = new MutableLiveData<>();

    private Handler mHandler = new Handler();

    public enum ConnState {
        CONNECTING,
        CONNECTED,
        INIT,
        READY,
        DISCONNECTING,
        DISCONNECTED
    }

    public DfuConnection(Context ctx) {
        this.mCtx = ctx;
        connection = new BLEConnectManager(ctx, mCallbacks);
        dfu.setPduSender(sender);
    }

    public DfuConnection(Context ctx, BluetoothDevice device) {
        this.mCtx = ctx;
        connection = new BLEConnectManager(ctx, mCallbacks);
        this.device = device;
        dfu.setPduSender(sender);
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public EasyDfu getDfu() {
        return dfu;
    }

    public LiveData<ConnState> getConnectionState() {
        return connectionState;
    }

    public LiveData<String> getErrorInfo() {
        return errorInfo;
    }

    public void requestConnect() {
        if (device == null) {
            return;
        }
        connectionState.postValue(ConnState.CONNECTING);
        connection.connect(device, false, 30_000);
    }

    public void requestDisconnect() {
        if (device == null) {
            return;
        }
        connectionState.postValue(ConnState.DISCONNECTING);
        connection.disconnect();
    }

    private IFrameSender sender = new IFrameSender() {

        @Override
        public boolean sendFrame(byte[] bytes) {
            if (connectionState.getValue() == ConnState.READY) {
                Log.v(TAG, "sendFrame()" + HexUtil.encodeHexStr(bytes));
                mDfuWriteCharacteristic.setValue(bytes);
                connection.writeCharacteristic(mDfuWriteCharacteristic, bytes, mDfuWriteCharacteristic.getWriteType());
                return true;
            }
            return false;
        }
    };

    private BLEConnectCallback mCallbacks = new BLEConnectCallback() {

//        private static final String TAG = "++++++";

        @Override
        public void onBleDisconnected() {
            Log.d(TAG, "onBleDisconnected() called");
            connectionState.postValue(ConnState.DISCONNECTED);
        }

        @Override
        public void onBleConnected() {
            Log.d(TAG, "onBleConnected() called");
            connectionState.postValue(ConnState.CONNECTED);
        }

        @Override
        public void onBleServicesDiscovered(BluetoothGatt gatt) {
            Log.d(TAG, "onBleServicesDiscovered() called with: gatt = [" + gatt + "]");
            final BluetoothGattService service = gatt.getService(DFU_SERVICE_UUID);
            if (service != null) {
                mDfuReceiveCharacteristic = service.getCharacteristic(DFU_RECEIVE_CHARACTERISTIC_UUID);
                mDfuWriteCharacteristic = service.getCharacteristic(DFU_WRITE_CHARACTERISTIC_UUID);
            }
            if (mDfuReceiveCharacteristic != null && mDfuWriteCharacteristic != null) {
                connection.enableNotify(mDfuReceiveCharacteristic);
                connectionState.postValue(DfuConnection.ConnState.INIT);
            } else {
                errorInfo.postValue("Device Not Support");
                requestDisconnect();
            }
        }

        @Override
        public void onBleCharacteristicNotify(BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onBleCharacteristicNotify() called with: characteristic = [" + characteristic.getUuid() + "]" + HexUtil.encodeHexStr(characteristic.getValue()));
            if (characteristic.equals(mDfuReceiveCharacteristic)) {
                dfu.onRcvPduSegment(characteristic.getValue());
            }
        }

        @Override
        public void onBleNotifyEnable() {
            Log.d(TAG, "onBleNotifyEnable() called");
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    connection.changeMtu(MTU_SEND_SIZE);
                }
            }, 2000);
        }

        @Override
        public void onBleCharacteristicWriteComplete(BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onBleCharacteristicWriteComplete() called with: characteristic = [" + characteristic.getUuid() + "]" + HexUtil.encodeHexStr(characteristic.getValue()));
            dfu.onSentPduSegment();
        }

        @Override
        public void onBleMtuChanged(int mtu) {
            Log.d(TAG, "onBleMtuChanged() called with: mtu = [" + mtu + "]");
            dfu.setMaxSegmentSize(mtu - 3);
            connectionState.postValue(ConnState.READY);
        }

        @Override
        public void onBleError(String message, int errorCode) {
            Log.d(TAG, "onBleError() called with: message = [" + message + "], errorCode = [" + errorCode + "]");
            errorInfo.postValue(message);
        }

        @Override
        public void onBleTimeOut(String type) {
            Log.d(TAG, "onBleTimeOut() called with: type = [" + type + "]");
            errorInfo.postValue("onBleTimeOut() called with: type = [" + type + "]");
        }
    };
}
