package com.szabh.smable3.entity

data class BleAlarm(
    var mEnabled: Int = 1,
    var mRepeat: Int = 0,
    var mYear: Int = 0,
    var mMonth: Int = 0,
    var mDay: Int = 0,
    var mHour: Int = 0,
    var mMinute: Int = 0,
    var mTag: String = ""
) : BleIdObject() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mId)
        writeIntN(mEnabled, 1)
        writeIntN(mRepeat, 7)
        writeInt8(mYear - 2000)
        writeInt8(mMonth)
        writeInt8(mDay)
        writeInt8(mHour)
        writeInt8(mMinute)
        writeStringWithFix(mTag, TAG_LENGTH)
    }

    override fun decode() {
        super.decode()
        mId = readUInt8().toInt()
        mEnabled = readIntN(1).toInt()
        mRepeat = readIntN(7).toInt()
        mYear = readUInt8().toInt() + 2000
        mMonth = readUInt8().toInt()
        mDay = readUInt8().toInt()
        mHour = readUInt8().toInt()
        mMinute = readUInt8().toInt()
        mTag = readString(TAG_LENGTH)
    }

    override fun toString(): String {
        return "BleAlarm(mAlarmId=$mId, mEnabled=$mEnabled, mRepeat=$mRepeat, mYear=$mYear" +
            ", mMonth=$mMonth, mDay=$mDay, mHour=$mHour, mMinute=$mMinute, mTag='$mTag')"
    }

    companion object {
        const val ITEM_LENGTH = 28

        private const val TAG_LENGTH = 21
    }
}
