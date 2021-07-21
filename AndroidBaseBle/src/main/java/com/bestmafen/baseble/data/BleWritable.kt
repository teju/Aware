package com.bestmafen.baseble.data

import androidx.annotation.CallSuper
import java.nio.ByteOrder
import java.nio.charset.Charset
import kotlin.experimental.or
import kotlin.math.min

/**
 * 工具类，为方便从[Any]转换为[ByteArray]。
 */
open class BleWritable : BleReadable(), BleBuffer {
    open val mLengthToWrite: Int
        get() = 0

    override fun toByteArray(): ByteArray {
        encode()
        return mBytes!!
    }

    @CallSuper
    open fun encode() {
        resetOffset()
        mBytes = ByteArray(mLengthToWrite)
    }

    /**
     * 写入[Int]的低n位。
     */
    fun writeIntN(value: Int, @androidx.annotation.IntRange(from = 1, to = 8) n: Int) {
        if (n < 1 || n > 8 || outOfRange(n)) return

        // 截取value的后n位
        val valueToWrite = value and (2 shl n shr 1) - 1
        if (mPositions[1] + n <= 8) { // 没有跨字节
            val shift = 8 - (mPositions[1] + n)
            mBytes!![mPositions[0]] = mBytes!![mPositions[0]] or (valueToWrite shl shift).toByte()
        } else { // 已经跨字节
            val shift = mPositions[1] + n - 8 // 跨过的位数
            mBytes!![mPositions[0]] = mBytes!![mPositions[0]] or (valueToWrite shr shift).toByte()
            mBytes!![mPositions[0] + 1] = mBytes!![mPositions[0] + 1] or (valueToWrite and (2 shl shift shr 1) - 1 shl 8 - shift).toByte()
        }
        skip(n)
    }

    /**
     * 写入[Boolean]，只写入1位，true写入1，否则写入0。
     */
    fun writeBoolean(value: Boolean) {
        writeIntN(if (value) 1 else 0, 1)
    }

    /**
     * 写入[Int]的低8位。
     */
    fun writeInt8(value: Int) {
        writeIntN(value, 8)
    }

    /**
     * 写入[Int]的低16位。
     */
    fun writeInt16(value: Int, order: ByteOrder = mByteOrder) {
        val high: Int
        val low: Int
        if (order == ByteOrder.BIG_ENDIAN) {
            high = value shr 8 and 0xff
            low = value and 0xff
        } else {
            low = value shr 8 and 0xff
            high = value and 0xff
        }
        writeInt8(high)
        writeInt8(low)
    }

    /**
     * 写入[Int]的低24位。
     */
    fun writeInt24(value: Int, order: ByteOrder = mByteOrder) {
        if (order == ByteOrder.BIG_ENDIAN) {
            writeInt16(value shr 8, order)
            writeInt8(value and 0xff)
        } else {
            writeInt16(value and 0xffff, order)
            writeInt8(value shr 16)
        }
    }

    /**
     * 写入[Int]，32位。
     */
    fun writeInt32(value: Int, order: ByteOrder = mByteOrder) {
        if (order == ByteOrder.BIG_ENDIAN) {
            writeInt16(value shr 16, order)
            writeInt16(value and 0xffff, order)
        } else {
            writeInt16(value and 0xffff, order)
            writeInt16(value shr 16, order)
        }
    }

    /**
     * 写入[Long]的低32位。
     */
    fun writeUInt32(value: Long, order: ByteOrder = mByteOrder) {
        writeInt32(value.toInt(), order)
    }

    /**
     * 写入[Long]，64位。
     */
    fun writeLong(value: Long, order: ByteOrder = mByteOrder) {
        if (order == ByteOrder.BIG_ENDIAN) {
            writeInt32((value shr 32).toInt(), order)
            writeInt32(value.toInt(), order)
        } else {
            writeInt32(value.toInt(), order)
            writeInt32((value shr 32).toInt(), order)
        }
    }

    /**
     * 写入[Float]，32位。
     */
    fun writeFloat(value: Float, order: ByteOrder = mByteOrder) {
        val bits = java.lang.Float.floatToIntBits(value)
        writeInt32(bits, order)
    }

    /**
     * 写入[Double]，64位。
     */
    fun writeDouble(value: Double, order: ByteOrder = mByteOrder) {
        val bits = java.lang.Double.doubleToLongBits(value)
        writeLong(bits, order)
    }

    /**
     * 写入[ByteArray]。
     */
    fun writeBytes(bytes: ByteArray?, order: ByteOrder = mByteOrder) {
        if (bytes == null) return

        if (order == ByteOrder.BIG_ENDIAN) {
            bytes.forEach {
                writeInt8(it.toInt())
            }
        } else {
            bytes.reversed().forEach {
                writeInt8(it.toInt())
            }
        }
    }

    /**
     * 写入[String]，编码后是多长，就写多长。
     *
     * @param text    待写入的字符串。
     * @param charSet 编码格式。
     */
    fun writeString(text: String?, charSet: Charset = Charset.defaultCharset()) {
        if (text == null || text.isEmpty()) return

        val bytes = text.toByteArray(charSet)
        writeBytes(bytes)
    }

    /**
     * 写入[String]，如果没有超过限定长度，编码后是多长写入多长，否则只写入限定的长度。
     *
     * @param text    待写入的字符串。
     * @param limit   限定最大分配的字节数。
     * @param charSet 编码格式。
     */
    fun writeStringWithLimit(text: String?, limit: Int, charSet: Charset = Charset.defaultCharset()) {
        if (text == null || text.isEmpty()) return

        //skip(8 - mPositions[1]); // 写入的字符序列不允许跨字节，先跳到下个字节的开始位置
        val bytes = text.toByteArray(charSet)
        val length = min(bytes.size, limit)
        writeBytes(bytes.sliceArray(0 until length))
    }

    /**
     * 写入[String]，不管编码后多长，都只写入固定长度。
     * 如果没有达到固定长度，剩余的会写入0；
     * 如果超过的固定长度，超过的会被忽略；
     *
     * @param text    待写入的字符串。
     * @param fix     分配的固定长度。
     * @param charSet 编码格式。
     */
    fun writeStringWithFix(text: String?, fix: Int, charSet: Charset = Charset.defaultCharset()) {
        if (text == null || text.isEmpty()) {
            skip(fix * 8)
            return
        }

        val bytes = text.toByteArray(charSet)
        val length = min(bytes.size, fix)
        writeBytes(bytes.sliceArray(0 until length))
        if (bytes.size < fix) {
            writeBytes(ByteArray(fix - bytes.size) { 0 })
        }
    }

    /**
     * 写入对象。
     */
    fun writeObject(buf: BleBuffer?) {
        if (buf == null) return

        writeBytes(buf.toByteArray())
    }

    /**
     * 写入列表。
     */
    fun writeList(list: List<BleBuffer>?) {
        list?.forEach {
            writeObject(it)
        }
    }
}
