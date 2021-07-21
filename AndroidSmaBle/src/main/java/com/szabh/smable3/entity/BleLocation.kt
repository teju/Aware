package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

data class BleLocation(
    var mTime: Int = 0, // 距离当地2000/1/1 00:00:00的秒数
    var mActivityMode: Int = 0,
    var mAltitude: Int = 0, // m
    var mLongitude: Float = 0f,
    var mLatitude: Float = 0f
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readInt32()
        mActivityMode = readUInt8().toInt()
        skip(8)
        mAltitude = readInt16().toInt()
        mLongitude = readFloat()
        mLatitude = readFloat()
    }

    companion object {
        const val ITEM_LENGTH = 16
    }
}
