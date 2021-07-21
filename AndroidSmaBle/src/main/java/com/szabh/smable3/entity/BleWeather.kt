package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable
import java.nio.ByteOrder

data class BleWeather(
    var mCurrentTemperature: Int = 0, // 摄氏度，for BleWeatherRealtime
    var mMaxTemperature: Int = 0,     // 摄氏度，for BleWeatherForecast
    var mMinTemperature: Int = 0,     // 摄氏度，for BleWeatherForecast
    var mWeatherCode: Int = 0,        // 天气类型，for both
    var mWindSpeed: Int = 0,          // m/s，for both
    var mHumidity: Int = 0,           // %，for both
    var mVisibility: Int = 0,         // km，for both
    // https://en.wikipedia.org/wiki/Ultraviolet_index
    // https://zh.wikipedia.org/wiki/%E7%B4%AB%E5%A4%96%E7%BA%BF%E6%8C%87%E6%95%B0
    // [0, 2] -> low, [3, 5] -> moderate, [6, 7] -> high, [8, 10] -> very high, >10 -> extreme
    var mUltraVioletIntensity: Int = 0, // 紫外线强度，for BleWeatherForecast
    var mPrecipitation: Int = 0         // 降水量 mm，for both
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mCurrentTemperature)
        writeInt8(mMaxTemperature)
        writeInt8(mMinTemperature)
        writeInt8(mWeatherCode)
        writeInt8(mWindSpeed)
        writeInt8(mHumidity)
        writeInt8(mVisibility)
        writeInt8(mUltraVioletIntensity)
        writeInt16(mPrecipitation, ByteOrder.LITTLE_ENDIAN)
    }

    override fun toString(): String {
        return "BleWeather(mCurrentTemperature=$mCurrentTemperature, mMaxTemperature=$mMaxTemperature, " +
            "mMinTemperature=$mMinTemperature, mWeatherCode=$mWeatherCode, mWindSpeed=$mWindSpeed, " +
            "mHumidity=$mHumidity, mVisibility=$mVisibility, mUltraVioletIntensity=$mUltraVioletIntensity, " +
            "mPrecipitation=$mPrecipitation)"
    }

    companion object {
        const val ITEM_LENGTH = 10

        // weather types
        const val SUNNY = 1
        const val CLOUDY = 2
        const val OVERCAST = 3
        const val RAINY = 4
        const val THUNDER = 5
        const val THUNDERSHOWER = 6
        const val HIGH_WINDY = 7
        const val SNOWY = 8
        const val FOGGY = 9
        const val SAND_STORM = 10
        const val HAZE = 11
        const val OTHER = 0
    }
}
