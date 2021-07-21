package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable
import java.util.*

data class BleTime(
    var mYear: Int = 0,
    var mMonth: Int = 0,
    var mDay: Int = 0,
    var mHour: Int = 0,
    var mMinute: Int = 0,
    var mSecond: Int = 0
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    private constructor(calendar: Calendar) : this(
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH] + 1,
        calendar[Calendar.DAY_OF_MONTH],
        calendar[Calendar.HOUR_OF_DAY],
        calendar[Calendar.MINUTE],
        calendar[Calendar.SECOND]
    )

    override fun encode() {
        super.encode()
        writeInt8(mYear - 2000)
        writeInt8(mMonth)
        writeInt8(mDay)
        writeInt8(mHour)
        writeInt8(mMinute)
        writeInt8(mSecond)
    }

    override fun decode() {
        super.decode()
        mYear = readUInt8().toInt() + 2000
        mMonth = readUInt8().toInt()
        mDay = readUInt8().toInt()
        mHour = readUInt8().toInt()
        mMinute = readUInt8().toInt()
        mSecond = readUInt8().toInt()
    }

    override fun toString(): String {
        return "BleTime(mYear=$mYear, mMonth=$mMonth, mDay=$mDay, mHour=$mHour, mMinute=$mMinute, mSecond=$mSecond)"
    }

    companion object {
        const val ITEM_LENGTH = 6

        val utcTimeZone: TimeZone
            get() = TimeZone.getTimeZone("GMT+0")

        fun utc(): BleTime {
            val calendar = Calendar.getInstance().also { it.timeZone = utcTimeZone }
            return BleTime(calendar)
        }

        fun local(): BleTime {
            val calendar = Calendar.getInstance()
            return BleTime(calendar)
        }

        fun ofLocal(timeInMillis: Long): BleTime {
            val calendar = Calendar.getInstance().also { it.timeInMillis = timeInMillis }
            return BleTime(calendar)
        }
    }
}
