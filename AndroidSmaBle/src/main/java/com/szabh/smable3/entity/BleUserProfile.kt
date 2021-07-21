package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable
import java.nio.ByteOrder

data class BleUserProfile(
    var mUnit: Int = METRIC,
    var mGender: Int = FEMALE,
    var mAge: Int = 20,
    var mHeight: Float = 170f,
    var mWeight: Float = 60f
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mUnit)
        writeInt8(mGender)
        writeInt8(mAge)
        writeFloat(mHeight, ByteOrder.LITTLE_ENDIAN)
        writeFloat(mWeight, ByteOrder.LITTLE_ENDIAN)
    }

    override fun decode() {
        super.decode()
        mUnit = readUInt8().toInt()
        mGender = readUInt8().toInt()
        mAge = readUInt8().toInt()
        mHeight = readFloat(ByteOrder.LITTLE_ENDIAN)
        mWeight = readFloat(ByteOrder.LITTLE_ENDIAN)
    }

    companion object {
        const val ITEM_LENGTH = 11

        const val METRIC = 0
        const val IMPERIAL = 1

        const val FEMALE = 0
        const val MALE = 1
    }
}
