package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable
import java.util.*

class BleTimeZone : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    private var mOffset: Int // 毫秒数

    init {
        mOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis())
    }

    override fun encode() {
        super.encode()
        writeInt8(mOffset / 1000 / 60 / 15)
    }

    override fun decode() {
        super.decode()
        mOffset = readInt8().toInt() * 1000 * 60 * 15
    }

    override fun toString(): String {
        return "BleTimeZone(mOffset=$mOffset)"
    }

    companion object {
        const val ITEM_LENGTH = 1
    }
}
