package com.bestmafen.baseble.scanner

import android.bluetooth.BluetoothDevice
import com.bestmafen.baseble.data.mHexString

/**
 * [BluetoothDevice]的包装类，扫描返回的结果。
 *
 * A wrapper of [BluetoothDevice], presenting a result of scanning.
 */
data class BleDevice(
    /**
     * 扫描到的蓝牙设备。
     *
     * Remote device.
     */
    var mBluetoothDevice: BluetoothDevice,

    /**
     * 蓝牙设备报告的信号强度。
     *
     * Received signal strength.
     */
    var mRssi: Int,

    /**
     * 蓝牙设备广播的数据。
     *
     * The content of the advertisement record offered by the remote device.
     */
    var mScanRecord: ByteArray?
) {

    override fun hashCode(): Int {
        return mBluetoothDevice.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is BleDevice) return this.mBluetoothDevice == other.mBluetoothDevice

        return false
    }

    override fun toString(): String {
        return "BleDevice(name=${mBluetoothDevice.name}, address=${mBluetoothDevice.address}, mRssi=$mRssi" +
            ", mScanRecord=${mScanRecord.mHexString})"
    }
}