package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleGestureWake(
    var mBleTimeRange: BleTimeRange = BleTimeRange()
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeObject(mBleTimeRange)
    }

    override fun decode() {
        super.decode()
        mBleTimeRange = readObject(BleTimeRange.ITEM_LENGTH)
    }

    companion object {
        const val ITEM_LENGTH = 5
    }
}