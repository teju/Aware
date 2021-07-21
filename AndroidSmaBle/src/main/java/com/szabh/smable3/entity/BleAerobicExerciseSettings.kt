package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleAerobicExerciseSettings(
    var mExerciseHour: Int = 0, // 运动时间（时）0~23小时
    var mExerciseMinute: Int = 0,//运动时间（分）0~59分
    var mHrMin: Int = 0,//心率下限值
    var mHrMax: Int = 0,//心率上限值
    var mLowHrMinMinute: Int = 0,//低于下限心率值时间（分）1~30分
    var mLowHrMinVibration: Int = 0,//低于下限心率值震动次数 1-4
    var mHighHrMaxMinute: Int = 0,//高于上限心率值时间（分）1~30分
    var mHighHrMaxVibration: Int = 0,//高于上限心率值震动次数 1-4
    var mLowOrHighHrDuration: Int = 0,//低于/高于设置心率值持续时间 1~30分
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mExerciseHour)
        writeInt8(mExerciseMinute)
        writeInt8(mHrMin)
        writeInt8(mHrMax)
        writeInt8(mLowHrMinMinute)
        writeInt8(mLowHrMinVibration)
        writeInt8(mHighHrMaxMinute)
        writeInt8(mHighHrMaxVibration)
        writeInt8(mLowOrHighHrDuration)
    }

    override fun decode() {
        super.decode()
        mExerciseHour = readUInt8().toInt()
        mExerciseMinute = readUInt8().toInt()
        mHrMin = readUInt8().toInt()
        mHrMax = readUInt8().toInt()
        mLowHrMinMinute = readUInt8().toInt()
        mLowHrMinVibration = readUInt8().toInt()
        mHighHrMaxMinute = readUInt8().toInt()
        mHighHrMaxVibration = readUInt8().toInt()
        mLowOrHighHrDuration = readUInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 9
    }
}
