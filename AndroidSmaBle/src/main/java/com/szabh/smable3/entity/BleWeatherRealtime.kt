package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleWeatherRealtime(
    var mTime: Int, // 时间戳，秒数
    var mWeather: BleWeather? = null
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeObject(BleTime.ofLocal(mTime * 1000L))
        writeObject(mWeather)
    }

    override fun toString(): String {
        return "BleWeatherRealtime(mTime=${BleTime.ofLocal(mTime * 1000L)}, mWeather=$mWeather)"
    }

    companion object {
        const val ITEM_LENGTH = BleTime.ITEM_LENGTH + BleWeather.ITEM_LENGTH
    }
}
