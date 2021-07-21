package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleGirlCareSettings(
    var mEnabled: Int = 0,
    var mReminderHour: Int = 0, // 提醒时间
    var mReminderMinute: Int = 0,
    var mMenstruationReminderAdvance: Int = 0, // 生理期提醒提前天数
    var mOvulationReminderAdvance: Int = 0, // 排卵期提醒提前天数
    var mLatestYear: Int = 0, // 上次生理期日期
    var mLatestMonth: Int = 0,
    var mLatestDay: Int = 0,
    var mMenstruationDuration: Int = 0, // 生理期持续时间，天
    var mMenstruationPeriod: Int = 0 // 生理期周期，天
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mEnabled)
        writeInt8(mReminderHour)
        writeInt8(mReminderMinute)
        writeInt8(mMenstruationReminderAdvance)
        writeInt8(mOvulationReminderAdvance)
        writeInt8(mLatestYear - 2000)
        writeInt8(mLatestMonth)
        writeInt8(mLatestDay)
        writeInt8(mMenstruationDuration)
        writeInt8(mMenstruationPeriod)
    }

    companion object {
        const val ITEM_LENGTH = 10
    }
}
