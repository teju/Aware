package com.szabh.androiddfu.goodix.ble_connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BLEConnectManager {
    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private final static String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
    private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private final static String ERROR_MTU_REQUEST = "Error on mtu request";

    public final static String TIME_OUT_CONNECT = "Timeout on connect";
    public final static String TIME_OUT_DISCVERY_SERVICE = "Timeout on discovery service";
    public final static String TIME_OUT_ENABLE_NOTIFY = "Timeout on enable_notify";
    public final static String TIME_OUT_MTU_CHANGE = "Timeout on mtu request";

    public static final int WRITE_CHARACTER_TIME_OUT = 3;

    private static final int DISCOVERY_SERVICE_TIME = 10000;
    private static final int ENABLE_NOTIFY_TIME = 5000;
    private static final int CHANGE_MTU_TIME = 5000;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt = null;
    private Context mContext;
    private BLEConnectCallback mCallbacks;
    private boolean mConnectionState = false;
    private Timer timeOutTimer;
    private String timeOutType;

    public BLEConnectManager(Context context, BLEConnectCallback mCallbacks) {
        this.mCallbacks = mCallbacks;
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    public void connect(BluetoothDevice device, boolean autoConnect, int connectTimeout) {
        if (device == null)
            return;
        startTimeOut(connectTimeout, TIME_OUT_CONNECT);
        mBluetoothGatt = device.connectGatt(mContext, autoConnect, mGattCallback);
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                BluetoothGatt gatt = mBluetoothGatt;
                if (gatt != null) {
                    mBluetoothGatt.close();
                }
            }
        }, 5000);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = true;
                stopTimeOut();
                mCallbacks.onBleConnected();
                startTimeOut(DISCOVERY_SERVICE_TIME, TIME_OUT_DISCVERY_SERVICE);
                gatt.discoverServices();
            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    stopTimeOut();
                    mCallbacks.onBleDisconnected();
                    gatt.close();
                } else {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        mCallbacks.onBleError(ERROR_CONNECTION_STATE_CHANGE, status);
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            stopTimeOut();
            if (status == BluetoothGatt.GATT_SUCCESS)
                mCallbacks.onBleServicesDiscovered(gatt);
            else
                mCallbacks.onBleError(ERROR_DISCOVERY_SERVICE, status);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                mCallbacks.onBleCharacteristicWriteComplete(characteristic);
            else
                mCallbacks.onBleError(ERROR_WRITE_CHARACTERISTIC, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mCallbacks.onBleCharacteristicNotify(characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (descriptor != null && CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID.equals(descriptor.getUuid())) {
                    final byte[] value = descriptor.getValue();
                    if (value != null && value.length == 2 && value[1] == 0x00) {
                        if (value[0] == 0x01) {
                            stopTimeOut();
                            mCallbacks.onBleNotifyEnable();
                        }
                    }
                }
            } else {
                mCallbacks.onBleError(ERROR_WRITE_DESCRIPTOR, status);
            }

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            stopTimeOut();
//            if (status == BluetoothGatt.GATT_SUCCESS)
            mCallbacks.onBleMtuChanged(mtu);
//            else
//                mCallbacks.onBleError(ERROR_MTU_REQUEST, status);
        }
    };

    private void startTimeOut(int timeout, String type) {
        timeOutType = type;
        timeOutTimer = null;
        timeOutTimer = new Timer();
        timeOutTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                timeOutTimer = null;
                Log.i("sss", "timeout");
                mCallbacks.onBleTimeOut(timeOutType);
            }
        }, timeout);
    }

    private void stopTimeOut() {
        if (timeOutTimer != null) {
            timeOutTimer.cancel();
            timeOutTimer = null;
        }
    }

    /**
     * write data to characteristic
     */
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;
        characteristic.setValue(data);
        characteristic.setWriteType(writeType);

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
            return false;
        return gatt.writeCharacteristic(characteristic);
    }

    /**
     * enable characteristic notification
     */
    public boolean enableNotify(BluetoothGattCharacteristic characteristic) {
        startTimeOut(ENABLE_NOTIFY_TIME, TIME_OUT_ENABLE_NOTIFY);
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;
        gatt.setCharacteristicNotification(characteristic, true);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor == null)
            return false;

        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();
        final int originalWriteType = parentCharacteristic.getWriteType();
        parentCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        final boolean result = mBluetoothGatt.writeDescriptor(descriptor);
        parentCharacteristic.setWriteType(originalWriteType);
        return result;
    }

    public boolean changeMtu(int mtu) {
        if (mtu < 23)
            mtu = 23;
        if (mtu > 517)
            mtu = 517;
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null)
            return false;
        startTimeOut(CHANGE_MTU_TIME, TIME_OUT_MTU_CHANGE);
        return gatt.requestMtu(mtu);
    }
}
