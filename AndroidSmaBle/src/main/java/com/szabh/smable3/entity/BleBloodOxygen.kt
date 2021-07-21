package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

/**
 * 血氧数据。
 */
data class BleBloodOxygen(
    var mTime: Int = 0, // 距离当地2000/1/1 00:00:00的秒数
    var mValue: Int = 0 // 血氧值，如果传输过来的是50，则最终显示为 50% 即可
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readInt32()
        mValue = readInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
