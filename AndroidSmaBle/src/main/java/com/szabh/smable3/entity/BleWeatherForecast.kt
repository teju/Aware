package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleWeatherForecast(
    var mTime: Int, // 时间戳，秒数
    var mWeather1: BleWeather? = null,
    var mWeather2: BleWeather? = null,
    var mWeather3: BleWeather? = null
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeObject(BleTime.ofLocal(mTime * 1000L))
        writeObject(mWeather1)
        writeObject(mWeather2)
        writeObject(mWeather3)
    }

    override fun toString(): String {
        return "BleWeatherForecast(mTime=${BleTime.ofLocal(mTime * 1000L)}, mWeather1=$mWeather1" +
            ", mWeather2=$mWeather2, mWeather3=$mWeather3)"
    }

    companion object {
        const val ITEM_LENGTH = BleTime.ITEM_LENGTH + BleWeather.ITEM_LENGTH * 3
    }
}
