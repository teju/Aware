package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

// 汇总式的锻炼数据
data class BleWorkout(
    var mStart: Int = 0,        //开始时间， 距离当地2000/1/1 00:00:00的秒数
    var mEnd: Int = 0,          //结束时间， 距离当地2000/1/1 00:00:00的秒数
    var mDuration: Int = 0,     //运动持续时长，数据以秒为单位
    var mAltitude: Int = 0,     //海拔高度，数据以米为单位
    var mAirPressure: Int = 0,  //气压，数据以 kPa 为单位
    var mSpm: Int = 0,          //步频，步数/分钟的值，直接可用
    var mMode: Int = 0,         //运动类型，与 BleActivity 中的 mMode 定义一致
    var mStep: Int = 0,         //步数，与 BleActivity 中的 mStep 定义一致
    var mDistance: Int = 0,     //米，以米为单位，例如接收到的数据为56045，则代表 56045 米 约等于 56.045 Km
    var mCalorie: Int = 0,      //卡，以卡为单位，例如接收到的数据为56045，则代表 56.045 Kcal 约等于 56 Kcal
    var mSpeed: Int = 0,        //速度，接收到的数据以 米/小时 为单位
    var mPace: Int = 0          //配速，接收道德数据以 秒/千米 为单位
) : BleReadable() {

    override fun decode() {
        super.decode()
        mStart = readInt32()
        mEnd = readInt32()
        mDuration = readUInt16().toInt()
        mAltitude = readInt16().toInt()
        mAirPressure = readUInt16().toInt()
        mSpm = readUInt8().toInt()
        mMode = readUInt8().toInt()
        mStep = readInt32()
        mDistance = readInt32()
        mCalorie = readInt32()
        mSpeed = readInt32()
        mPace = readInt32()
    }

    companion object {
        const val ITEM_LENGTH = 48
    }
}
