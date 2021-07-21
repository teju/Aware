package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

/**
 * 压力数据
 */
data class BlePressure(
    var mTime: Int = 0, // 距离当地2000/1/1 00:00:00的秒数
    var mValue: Int = 0
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readInt32()
        mValue = readUInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
