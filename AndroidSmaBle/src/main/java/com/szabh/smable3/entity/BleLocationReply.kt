package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

// 一些无Gps设备在锻炼时会请求手机定位，这个类是手机定位成功后对该指令的响应
data class BleLocationReply(
    var mSpeed: Float = 0f, // 千米/小时
    var mTotalDistance: Float = 0f, // 千米
    var mAltitude: Int = 0 // 米
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeFloat(mSpeed)
        writeFloat(mTotalDistance)
        writeInt16(mAltitude)
    }

    companion object {
        const val ITEM_LENGTH = 16
    }
}
