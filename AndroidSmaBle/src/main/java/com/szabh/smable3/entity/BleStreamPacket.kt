package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

/**
 * 当发送的内容长度超过固件接收buffer的长度时，需要拆分成多条指令发送
 */
class BleStreamPacket(
    private val mType: Int,
    private val mSize: Long,
    private val mIndex: Long,
    private val mPacket: ByteArray
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = 1 + 4 + 4 + mPacket.size

    override fun encode() {
        super.encode()
        writeInt8(mType)
        writeUInt32(mSize)
        writeUInt32(mIndex)
        writeBytes(mPacket)
    }

    override fun toString(): String {
        return "BleStreamPacket(mType=$mType, mSize=$mSize, mIndex=$mIndex, mPacket=${mPacket.size})"
    }

    companion object {
        const val BUFFER_MAX_SIZE = 480L
    }
}