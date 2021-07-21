package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

/**
 * 自动温度检测。
 */
data class BleTemperatureDetecting(
    var mBleTimeRange: BleTimeRange = BleTimeRange(),
    var mInterval: Int = 60 // 设备默认1小时检测1次; 分钟数，发送时除以5
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeObject(mBleTimeRange)
        writeInt8(mInterval / 5)
    }

    override fun decode() {
        super.decode()
        mBleTimeRange = readObject(BleTimeRange.ITEM_LENGTH)
        mInterval = readUInt8().toInt() * 5
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
