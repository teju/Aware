package com.szabh.androiddfu.goodix.ble_connect;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public interface BLEConnectCallback {

    void onBleDisconnected();

    void onBleConnected();

    void onBleServicesDiscovered(BluetoothGatt gatt);

    void onBleCharacteristicNotify(BluetoothGattCharacteristic characteristic);

    void onBleNotifyEnable();

    void onBleCharacteristicWriteComplete(BluetoothGattCharacteristic characteristic);

    void onBleMtuChanged(int mtu);

    void onBleError(final String message, final int errorCode);

    void onBleTimeOut(String type);

}
