package com.szabh.smable3.entity

import com.szabh.smable3.component.BleConnector
import kotlin.math.min

/**
 * 不支持[BleConnector.sendList]，所以不能一次创建多个，也不能执行重置操作，
 * 也不支持读取操作。
 */
data class BleSchedule(
    var mYear: Int = 0,
    var mMonth: Int = 0,
    var mDay: Int = 0,
    var mHour: Int = 0,
    var mMinute: Int = 0,
    var mAdvance: Int = 0, // 提前提醒分钟数, 0 ~ 0xffff
    var mTitle: String = "",
    var mContent: String = ""
) : BleIdObject() {
    override val mLengthToWrite: Int
        get() = 8 + TITLE_LENGTH + min(mContent.toByteArray().size, CONTENT_MAX_LENGTH)

    override fun encode() {
        super.encode()
        writeInt8(mId)
        writeInt8(mYear - 2000)
        writeInt8(mMonth)
        writeInt8(mDay)
        writeInt8(mHour)
        writeInt8(mMinute)
        writeInt16(mAdvance)
        writeStringWithFix(mTitle, TITLE_LENGTH)
        writeStringWithLimit(mContent, CONTENT_MAX_LENGTH)
        writeInt8(0)
    }

    override fun decode() {
        super.decode()
//        mScheduleId = readUInt8().toInt()
//        mYear = readUInt8().toInt() + 2000
//        mMonth = readUInt8().toInt()
//        mDay = readUInt8().toInt()
//        mHour = readUInt8().toInt()
//        mMinute = readUInt8().toInt()
//        mAdvance = readUInt16().toInt()
//        mTitle = readString(TITLE_LENGTH)
//        mContent = readString(CONTENT_LENGTH)
    }

    companion object {
        const val TITLE_LENGTH = 32
        const val CONTENT_MAX_LENGTH = 250
    }
}
