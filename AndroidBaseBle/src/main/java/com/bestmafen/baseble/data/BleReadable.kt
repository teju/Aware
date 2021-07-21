package com.bestmafen.baseble.data

import androidx.annotation.CallSuper
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.*

/**
 * 工具类，为方便从[ByteArray]转换为[Any]。
 */
open class BleReadable : BleByteArray() {

    @CallSuper
    open fun decode() {
        resetOffset()
    }

    /**
     * 读取n位，解析成[Byte]，如果n<8，则必定是一个非负数。
     * @return 范围为-0x80~0x7f。
     */
    fun readIntN(@androidx.annotation.IntRange(from = 1, to = 8) n: Int): Byte {
        if (n < 1 || n > 8 || outOfRange(n)) return 0

        var value: Int
        if (mPositions[1] + n <= 8) {//没有跨字节
            value = mBytes!![mPositions[0]].toInt() shr 8 - mPositions[1] - n
            value = value and (2 shl n shr 1) - 1
        } else {//已经跨字节
            value = mBytes!![mPositions[0]].toInt() and (2 shl (8 - mPositions[1]) shr 1) - 1
            value = value shl mPositions[1] + n - 8

            var value2 = mBytes!![mPositions[0] + 1].toInt() and 0xff
            value2 = value2 shr (16 - mPositions[1] - n)

            value = value or value2
        }
        skip(n)
        return value.toByte()
    }

    /**
     * 读取1位，解析成一个[Boolean]。
     * @return 如果该位为1，返回true，否则返回false。
     */
    fun readBoolean(): Boolean {
        return readIntN(1) == 1.toByte()
    }

    /**
     * 读取8位，解析成[Byte]。
     * @return 范围为-0x80~0x7f。
     */
    fun readInt8(): Byte {
        return readIntN(8)
    }

    /**
     * 读取8位，解析成[Char]。
     * @return 范围为0~0xff。
     */
    fun readUInt8(): Char {
        return readIntN(8).toInt().and(0xff).toChar()
    }

    /**
     * 读取16位，解析成[Short]。
     * @return 范围为-0x8000～0x7fff。
     */
    fun readInt16(order: ByteOrder = mByteOrder): Short {
        val high: Byte
        val low: Byte
        if (order == ByteOrder.BIG_ENDIAN) {
            high = readInt8()
            low = readInt8()
        } else {
            low = readInt8()
            high = readInt8()
        }
        return (high.toInt().and(0xff) shl 8 or low.toInt().and(0xff)).toShort()
    }

    /**
     * 读取16位，解析成[Char]。
     * @return 范围为0~0xffff。
     */
    fun readUInt16(order: ByteOrder = mByteOrder): Char {
        return readInt16(order).toChar()
    }

    /**
     * 读取24位，解析成[Int]，必定是一个非负数。
     * @return 范围为0~0xffffff。
     */
    fun readInt24(order: ByteOrder = mByteOrder): Int {
        val high: Char
        val low: Char
        if (order == ByteOrder.BIG_ENDIAN) {
            high = readUInt8()
            low = readUInt16(order)
        } else {
            low = readUInt16(order)
            high = readUInt8()
        }
        return high.toInt() shl 16 or low.toInt()
    }

    /**
     * 读取32位，解析成[Int]。
     * @return 范围为-0x80000000~0x7fffffff。
     */
    fun readInt32(order: ByteOrder = mByteOrder): Int {
        val high: Short
        val low: Short
        if (order == ByteOrder.BIG_ENDIAN) {
            high = readInt16(order)
            low = readInt16(order)
        } else {
            low = readInt16(order)
            high = readInt16(order)
        }
        return high.toInt().and(0xffff) shl 16 or (low.toInt().and(0xffff))
    }

    /**
     * 读取32位，解析成[Long]，必定为一个非负数。
     * @return 范围为0~0xffffffff。
     */
    fun readUInt32(order: ByteOrder = mByteOrder): Long {
        return readInt32(order).toLong().and(0xffffffff)
    }

    /**
     * 读取64位，解析成[Long]。
     * @return 范围为-0x8000000000000000~0x7fffffffffffffff。
     */
    fun readInt64(order: ByteOrder = mByteOrder): Long {
        val high: Int
        val low: Int
        if (order == ByteOrder.BIG_ENDIAN) {
            high = readInt32(order)
            low = readInt32(order)
        } else {
            low = readInt32(order)
            high = readInt32(order)
        }
        return high.toLong().and(0xffffffff) shl 32 or (low.toLong().and(0xffffffff))
    }

    /**
     * 读取32位，解析成[Float]。
     */
    fun readFloat(order: ByteOrder = mByteOrder): Float {
        val intBits = readInt32(order)
        return java.lang.Float.intBitsToFloat(intBits)
    }

    /**
     * 读取64位，解析成[Double]。
     */
    fun readDouble(order: ByteOrder = mByteOrder): Double {
        val longBits = readInt64(order)
        return java.lang.Double.longBitsToDouble(longBits)
    }

    /**
     * 读取n个字节。
     */
    fun readBytes(length: Int): ByteArray {
        val result = ByteArray(length)
        for (i in 0 until length) {
            result[i] = readInt8()
        }
        return result
    }

    /**
     * 读取n个字节，根据编码解析成[String]，如果最后一个字节为'\0'，会被忽略。
     */
    fun readString(length: Int, charset: Charset = Charset.defaultCharset()): String {
        return try {
            val bytes = readBytes(length)
            val endIndex = bytes.indexOfFirst { it == BYTE0 }
            if (endIndex == -1) {
                String(bytes, charset)
            } else {
                String(bytes.copyOf(endIndex), charset)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 读取n个字节，解析成对象。
     */
    inline fun <reified T : BleReadable> readObject(itemLength: Int): T {
        val t = T::class.java.newInstance()
        t.mBytes = readBytes(itemLength)
        t.decode()
        return t
    }

    /**
     * 读取count*itemLength个字节，解析成列表。
     */
    inline fun <reified T : BleReadable> readList(count: Int, itemLength: Int): MutableList<T> {
        val list = mutableListOf<T>()
        repeat(count) {
            list.add(readObject(itemLength))
        }
        return list
    }

    /**
     * 一直读取，直到遇到指定的数字。
     * 该方法不适用于跨字节的情况，返回的ByteArray不包含末尾的util，但是mPositions会移动到util的下个字节。
     */
    fun readBytesUtil(util: Byte): ByteArray {
        mBytes?.forEachIndexed { index, byte ->
            if (index >= mPositions[0] && byte == util) {
                val bytes = readBytes(index - mPositions[0])
                skip(8)
                return bytes
            }
        }

        return ByteArray(0)
    }

    /**
     * 一直读取，直到遇到指定的数字，然后将读取的数据根据编码转换成[String]并返回。
     * 该方法不适用于跨字节的情况，返回的ByteArray不包含末尾的util，但是mPositions会移动到util的下个字节。
     */
    fun readStringUtil(util: Byte, charset: Charset = Charset.defaultCharset()): String {
        return readBytesUtil(util).let {
            if (it.isEmpty()) {
                ""
            } else {
                String(it, charset)
            }
        }
    }

    companion object {
        private const val BYTE0 = 0.toByte()

        /**
         * 工厂方法，将[ByteArray]转换为[BleReadable]子类的实例。
         * @param from include。
         * @param to exclude。
         */
        inline fun <reified T : BleReadable> ofObject(bytes: ByteArray, from: Int = 0, to: Int = bytes.size): T {
            return T::class.java.newInstance().also {
                it.mBytes = bytes.copyOfRange(from, to)
                it.decode()
            }
        }

        /**
         * 工厂方法，将[ByteArray]转换为[BleReadable]子类的列表。
         * @param from include。
         * @param to exclude。
         */
        inline fun <reified T : BleReadable> ofList(
            bytes: ByteArray, itemLength: Int, from: Int = 0, to: Int = bytes.size): List<T> {
            return ArrayList<T>().also {
                val count = (to - from) / itemLength
                if (count > 0) {
                    for (i in 0 until count) {
                        it.add(ofObject(bytes.copyOfRange(from + itemLength * i, from + itemLength * (i + 1))))
                    }
                }
            }
        }
    }
}
