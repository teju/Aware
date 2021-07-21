package com.bestmafen.baseble.data

import java.nio.ByteOrder

val ByteArray?.mHexString: String
    get() {
        if (this == null) return ""

        return joinToString(", ") {
            String.format("0x%02X", it)
        }
    }

fun ByteArray?.append(other: ByteArray?): ByteArray {
    if (this == null && other == null) return ByteArray(0)
    if (this == null) return other!!
    if (other == null) return this
    return this + other
}

fun byteArrayOfBoolean(value: Boolean): ByteArray {
    return byteArrayOf(if (value) 1 else 0)
}

fun byteArrayOfInt8(value: Int): ByteArray {
    return byteArrayOf(value.toByte())
}

fun byteArrayOfInt16(value: Int, order: ByteOrder): ByteArray {
    val bytes = ByteArray(2)
    if (order == ByteOrder.BIG_ENDIAN) {
        bytes[0] = (value shr 8 and 0xff).toByte()
        bytes[1] = (value and 0xff).toByte()
    } else {
        bytes[1] = (value shr 8 and 0xff).toByte()
        bytes[0] = (value and 0xff).toByte()
    }
    return bytes
}

fun byteArrayOfInt24(value: Int, order: ByteOrder): ByteArray {
    val bytes = ByteArray(3)
    if (order == ByteOrder.BIG_ENDIAN) {
        bytes[0] = (value shr 16 and 0xff).toByte()
        bytes[1] = (value shr 8 and 0xff).toByte()
        bytes[2] = (value and 0xff).toByte()
    } else {
        bytes[2] = (value shr 16 and 0xff).toByte()
        bytes[1] = (value shr 8 and 0xff).toByte()
        bytes[0] = (value and 0xff).toByte()
    }
    return bytes
}

fun byteArrayOfInt32(value: Int, order: ByteOrder): ByteArray {
    val bytes = ByteArray(4)
    if (order == ByteOrder.BIG_ENDIAN) {
        bytes[0] = (value shr 24 and 0xff).toByte()
        bytes[1] = (value shr 16 and 0xff).toByte()
        bytes[2] = (value shr 8 and 0xff).toByte()
        bytes[3] = (value and 0xff).toByte()
    } else {
        bytes[0] = (value and 0xff).toByte()
        bytes[1] = (value shr 8 and 0xff).toByte()
        bytes[2] = (value shr 16 and 0xff).toByte()
        bytes[3] = (value shr 24 and 0xff).toByte()
    }
    return bytes
}

fun ByteArray.getInt(start: Int, length: Int, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Int {
    require(!(length < 1 || length > 4)) { "length must be in [1, 4]" }

    if (start + length > this.size) return 0

    return if (byteOrder == ByteOrder.BIG_ENDIAN) {
        when (length) {
            1 -> this[start].toInt() and 0xff
            2 -> (this[start].toInt() and 0xff shl 8).or(this[start + 1].toInt() and 0xff)
            3 -> (this[start].toInt() and 0xff shl 16).or(this[start + 1].toInt() and 0xff shl 8)
                .or(this[start + 2].toInt() and 0xff)
            4 -> (this[start].toInt() and 0xff shl 24).or(this[start + 1].toInt() and 0xff shl 16)
                .or(this[start + 2].toInt() and 0xff shl 8).or(this[start + 3].toInt() and 0xff)
            else -> 0
        }
    } else {
        when (length) {
            1 -> this[start].toInt() and 0xff
            2 -> (this[start].toInt() and 0xff).or(this[start + 1].toInt() and 0xff shl 8)
            3 -> (this[start].toInt() and 0xff).or(this[start + 1].toInt() and 0xff shl 8)
                .or(this[start + 2].toInt() and 0xff shl 16)
            4 -> (this[start].toInt() and 0xff).or(this[start + 1].toInt() and 0xff shl 8)
                .or(this[start + 2].toInt() and 0xff shl 16).or(this[start + 3].toInt() and 0xff shl 24)
            else -> 0
        }
    }
}

fun ByteArray?.splitWith0(beginAt: Int = 0): MutableList<ByteArray> {
    val result = mutableListOf<ByteArray>()
    var started = false
    var startIndex = 0
    this?.forEachIndexed { index, b ->
        if (index < beginAt) return@forEachIndexed

        if (b == 0.toByte()) {
            if (started) {
                result.add(copyOfRange(startIndex, index))
                started = false
            }
        } else {
            if (!started) {
                started = true
                startIndex = index
            }
            if (index == lastIndex) {
                if (started) {
                    result.add(copyOfRange(startIndex, index + 1))
                }
            }
        }
    }
    return result
}