package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleTimeRange(
    var mEnabled: Int = 0,
    var mStartHour: Int = 0,
    var mStartMinute: Int = 0,
    var mEndHour: Int = 0,
    var mEndMinute: Int = 0
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mEnabled)
        writeInt8(mStartHour)
        writeInt8(mStartMinute)
        writeInt8(mEndHour)
        writeInt8(mEndMinute)
    }

    override fun decode() {
        super.decode()
        mEnabled = readUInt8().toInt()
        mStartHour = readUInt8().toInt()
        mStartMinute = readUInt8().toInt()
        mEndHour = readUInt8().toInt()
        mEndMinute = readUInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 5
    }
}
