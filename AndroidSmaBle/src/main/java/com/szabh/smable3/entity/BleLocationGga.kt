package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable
import java.nio.ByteOrder

/**
 * 设备定位原始GGA数据
 */
data class BleLocationGga(
    var mTime: BleLocationUTC = BleLocationUTC(), // UTC时间
    var mLatitude: Float = 0f, // 纬度
    var mLongitude: Float = 0f, // 精度
    var positioningQuality: Int = 0, // 定位质量
    var satelliteCount: Int = 0, // 定位星数
    var mHorizontalDilutionPrecision: Float = 0.0f, // 水平分量精度因子, 纬度和经度等误差平方和的开根号值, 越小定位越准
    var altitude: Float = 0.0f, // 海拔, 单位: m
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readObject(BleLocationUTC.ITEM_LENGTH)
        mLatitude = readFloat(ByteOrder.LITTLE_ENDIAN)
        mLongitude = readFloat(ByteOrder.LITTLE_ENDIAN)
        positioningQuality = readInt32(ByteOrder.LITTLE_ENDIAN)
        satelliteCount = readInt32(ByteOrder.LITTLE_ENDIAN)
        mHorizontalDilutionPrecision = readFloat(ByteOrder.LITTLE_ENDIAN)
        altitude = readFloat(ByteOrder.LITTLE_ENDIAN)
    }

    companion object {
        const val ITEM_LENGTH = 64
    }
}
