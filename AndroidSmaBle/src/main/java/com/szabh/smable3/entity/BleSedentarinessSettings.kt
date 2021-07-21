package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleSedentarinessSettings(
    var mEnabled: Int = 0,
    var mRepeat: Int = 0,
    var mStartHour: Int = 0,
    var mStartMinute: Int = 0,
    var mEndHour: Int = 0,
    var mEndMinute: Int = 0,
    var mInterval: Int = 0 // 分钟数
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeIntN(mEnabled, 1)
        writeIntN(mRepeat, 7)
        writeInt8(mStartHour)
        writeInt8(mStartMinute)
        writeInt8(mEndHour)
        writeInt8(mEndMinute)
        writeInt8(mInterval)
    }

    override fun decode() {
        super.decode()
        mEnabled = readIntN(1).toInt()
        mRepeat = readIntN(7).toInt()
        mStartHour = readUInt8().toInt()
        mStartMinute = readUInt8().toInt()
        mEndHour = readUInt8().toInt()
        mEndMinute = readUInt8().toInt()
        mInterval = readUInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
