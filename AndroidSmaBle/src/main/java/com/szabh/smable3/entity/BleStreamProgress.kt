package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

data class BleStreamProgress(
    var mStatus: Int = 0,
    var mErrorCode: Int = 0, // 错误类型，未出错时忽略
    var mTotal: Int = 0,
    var mCompleted: Int = 0
) : BleReadable() {

    override fun decode() {
        super.decode()
        mStatus = readIntN(4).toInt()
        mErrorCode = readIntN(4).toInt()
        mTotal = readInt32()
        mCompleted = readInt32()
    }

    companion object {
        const val ITEM_LENGTH = 9
    }
}
