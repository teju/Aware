package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleHrMonitoringSettings(
    var mBleTimeRange: BleTimeRange = BleTimeRange(),
    var mInterval: Int = 60 //默认60分钟
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeObject(mBleTimeRange)
        writeInt8(mInterval)
    }

    override fun decode() {
        super.decode()
        mBleTimeRange = readObject(BleTimeRange.ITEM_LENGTH)
        mInterval = readUInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
