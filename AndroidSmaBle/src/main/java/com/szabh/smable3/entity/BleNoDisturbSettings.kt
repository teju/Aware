package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleNoDisturbSettings(
    var mEnabled: Int = 0,
    var mBleTimeRange1: BleTimeRange = BleTimeRange(),
    var mBleTimeRange2: BleTimeRange = BleTimeRange(),
    var mBleTimeRange3: BleTimeRange = BleTimeRange()
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mEnabled)
        writeObject(mBleTimeRange1)
        writeObject(mBleTimeRange2)
        writeObject(mBleTimeRange3)
    }

    override fun decode() {
        super.decode()
        mEnabled = readUInt8().toInt()
        mBleTimeRange1 = readObject(BleTimeRange.ITEM_LENGTH)
        mBleTimeRange2 = readObject(BleTimeRange.ITEM_LENGTH)
        mBleTimeRange3 = readObject(BleTimeRange.ITEM_LENGTH)
    }

    companion object {
        const val ITEM_LENGTH = 16
    }
}
