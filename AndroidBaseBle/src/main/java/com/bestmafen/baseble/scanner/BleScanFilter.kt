package com.bestmafen.baseble.scanner

/**
 * 设备扫描过滤器，用于指定匹配规则，来使得扫描时只报告那些我们需要关心的设备。
 */
interface BleScanFilter {

    /**
     * 判定一个设备是否需要加入到扫描结果中，即会不会触发[BleScanCallback.onDeviceFound]。
     *
     * @return 如果为true，加入到扫描结果，否则不加入到扫描结果。
     */
    fun match(device: BleDevice): Boolean
}

/**
 * [BleScanFilter]的一个简单实现类，只关心那个地址为mAddress的设备，不区分大小写。
 */
class AddressFilter(var mAddress: String) : BleScanFilter {

    override fun match(device: BleDevice): Boolean {
        return mAddress.equals(device.mBluetoothDevice.address, true)
    }
}