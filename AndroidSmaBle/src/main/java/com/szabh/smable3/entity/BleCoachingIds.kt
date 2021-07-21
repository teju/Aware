package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable
import java.nio.ByteOrder

class BleCoachingIds(
    var mCount: Int = 0,
    var mIds: MutableList<Int> = mutableListOf()
) : BleReadable() {

    override fun decode() {
        super.decode()
        mIds.clear()
        mCount = readInt8().toInt()
        repeat(mCount) {
            mIds.add(readInt8().toInt())
        }
    }

    override fun toString(): String {
        return "BleCoachingIds(mCount=$mCount, mIds=$mIds)"
    }
}