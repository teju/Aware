package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable
import java.nio.ByteOrder

/**
 * 睡眠质量数据
 * 部分设备不支持本地计算睡眠数据，
 * 需要通过App同步设备数据后，
 * 计算睡眠数据（可参考 BleSleep.analyseSleep 和 BleSleep.getSleepStatusDuration 方法），
 * 然后将这些数据回传给设备
 */
data class BleSleepQuality(
    var mLight: Int = 0, // 分钟数 浅睡
    var mDeep: Int = 0, // 分钟数  深睡
    var mTotal: Int = 0 // 分钟数  总睡眠时间
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt16(mLight, ByteOrder.LITTLE_ENDIAN)
        writeInt16(mDeep, ByteOrder.LITTLE_ENDIAN)
        writeInt16(mTotal, ByteOrder.LITTLE_ENDIAN)
    }

    override fun decode() {
        super.decode()
        mLight = readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()
        mDeep = readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()
        mTotal = readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()
    }

    companion object {
        const val ITEM_LENGTH = 6
    }
}
