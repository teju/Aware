package com.bestmafen.baseble.data

import java.nio.ByteOrder
import java.util.*

/**
 * 工具类，为方便[ByteArray]和[Any]之间的转换。
 */
open class BleByteArray(
    @Transient var mBytes: ByteArray? = null,
    @Transient var mByteOrder: ByteOrder = ByteOrder.BIG_ENDIAN
) {
    /**
     * 将要读取/写入的位置, 表示当前位置还未读取/写入。
     * [0]: 第几个字节, 范围为 [0, mBytes.size - 1]。
     * [1]: 当前字节的第几位，范围为 [0, 7]。
     * 初始位置为 [0, 0], 表示将要读第一位。
     * 当位置为 [mBytes.size - 1, 8] 时, 表示已经读/写完毕。
     * xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx
     * xxxxxxxx x^xxxxxx xxxxxxxx xxxxxxxx -> [0]=1, [1]=1
     */
    @Transient
    val mPositions = IntArray(2)

    /**
     * 获取当前的bits偏移。
     */
    fun bitsOffset(): Int {
        return mPositions[0] * 8 + mPositions[1]
    }

    /**
     *  剩余未读/写的位数。
     */
    fun bitsLeft(): Int {
        return if (mBytes == null) {
            0
        } else {
            mBytes!!.size * 8 - bitsOffset()
        }
    }

    /**
     * 判断如果偏移指定位数, 是否会越界, 该方法不会造成实际偏移。
     */
    fun outOfRange(bits: Int): Boolean {
        return if (mBytes == null) {
            true
        } else {
            val offset = bitsOffset() + bits
            offset < 0 || offset > mBytes!!.size * 8
        }
    }

    /**
     * 修改当前位置偏移。
     * @param offset 偏移的位数: 大于0时, 向后偏移; 小于0时, 向前偏移。
     */
    fun skip(offset: Int) {
        var newOffset = bitsOffset() + offset
        if (newOffset < 0) newOffset = 0
        mPositions[0] = newOffset / 8
        mPositions[1] = newOffset % 8
    }

    /**
     * 重置读/写的位置, 标记到开始位置, 即第0个字节第0位。
     */
    fun resetOffset() {
        Arrays.fill(mPositions, 0)
    }
}
