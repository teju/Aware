package com.bestmafen.baseble.connector

import android.bluetooth.BluetoothGattCallback

/**
 * 简化和转发[BluetoothGattCallback]的回调。
 */
interface BleGattCallback {

    /**
     * 连接状态变化，[BluetoothGattCallback.onConnectionStateChange]。
     */
    fun onConnectionStateChange(connected: Boolean) {}

    /**
     * 读，[BluetoothGattCallback.onCharacteristicRead]。
     */
    fun onCharacteristicRead(characteristicUuid: String, value: ByteArray, text: String) {}

    /**
     * 写，[BluetoothGattCallback.onCharacteristicWrite]。
     */
    fun onCharacteristicWrite(characteristicUuid: String, value: ByteArray) {}

    /**
     * 收到通知，[BluetoothGattCallback.onCharacteristicChanged]。
     */
    fun onCharacteristicChanged(value: ByteArray) {}

    /**
     * MTU变化，[BluetoothGattCallback.onMtuChanged]。
     */
    fun onMtuChanged() {}
}
