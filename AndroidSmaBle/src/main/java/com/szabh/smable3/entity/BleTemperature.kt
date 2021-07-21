package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

/**
 * 体温数据。
 */
data class BleTemperature(
    var mTime: Int = 0, // 距离当地2000/1/1 00:00:00的秒数
    var mTemperature: Int = 0 // 0.1摄氏度
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readInt32()
        mTemperature = readInt16().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
