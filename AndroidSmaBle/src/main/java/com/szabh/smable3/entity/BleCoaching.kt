package com.szabh.smable3.entity

import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.ID_ALL
import kotlin.math.min

/**
 * 不支持[BleConnector.sendList]，所以不能一次创建多个，也不能执行重置操作。
 * 也不支持[BleConnector.sendInt8]，删除时只需要删除本地缓存。
 * 读取时只支持[ID_ALL]，不支持读取单个，而且设备并不是返回该类的列表，而是[BleCoachingIds]对象，里面包含了设备上已存在的实例的id列表。
 */
data class BleCoaching(
    var mTitle: String = "",
    var mDescription: String = "",
    var mRepeat: Int = 1,
    var mSegments: List<BleCoachingSegment> = emptyList()
) : BleIdObject() {
    private val mDescriptionLength: Int
        get() = min(mDescription.toByteArray().size, MAX_LENGTH_DESCRIPTION)
    private val mSegmentsCount: Int
        get() = mSegments.size

    override val mLengthToWrite: Int
        get() = LENGTH_FIXED + mDescriptionLength + mSegments.sumBy { it.mLengthToWrite }

    override fun encode() {
        super.encode()
        writeInt8(mId)
        writeStringWithFix(mTitle, 15)
        writeInt8(mDescriptionLength)
        writeStringWithFix(mDescription, mDescriptionLength)
        writeInt8(mRepeat)
        writeInt8(mSegmentsCount)
        writeList(mSegments)
    }

    override fun toString(): String {
        return "BleCoaching(mId=$mId, mTitle='$mTitle', mDescription='$mDescription', mRepeat=$mRepeat" +
            ", mSegments=$mSegments)"
    }

    companion object {
        const val ITEM_LENGTH = 21

        const val LENGTH_TITLE = 15 // 包括结束的0，所以有效只有14字节
        const val LENGTH_FIXED = LENGTH_TITLE + 4
        const val MAX_LENGTH_DESCRIPTION = 128
    }
}
