package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

data class BleBloodPressure(
    var mTime: Int = 0, // 距离当地2000/1/1 00:00:00的秒数
    var mSystolic: Int = 0, // 收缩压
    var mDiastolic: Int = 0 // 舒张压
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readInt32()
        mSystolic = readUInt8().toInt()
        mDiastolic = readUInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
