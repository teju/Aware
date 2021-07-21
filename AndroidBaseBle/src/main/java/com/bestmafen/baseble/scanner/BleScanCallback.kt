package com.bestmafen.baseble.scanner

/**
 * 蓝牙扫描回调接口，所有方法都在主线程调用
 */
interface BleScanCallback {

    /**
     * 当开启扫描时，如果手机蓝牙未开启，会触发该方法，并且不会执行扫描操作
     */
    fun onBluetoothDisabled()

    /**
     * 开启或停止扫描时触发
     */
    fun onScan(scan: Boolean)

    /**
     * 扫描到设备时触发
     */
    fun onDeviceFound(device: BleDevice)
}
