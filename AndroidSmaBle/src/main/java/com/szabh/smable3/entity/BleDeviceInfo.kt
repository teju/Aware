package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable
import com.szabh.smable3.BleCommand
import com.szabh.smable3.BleKey
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.entity.BleDeviceInfo.Companion.AGPS_AGNSS
import com.szabh.smable3.entity.BleDeviceInfo.Companion.AGPS_EPO
import com.szabh.smable3.entity.BleDeviceInfo.Companion.AGPS_UBLOX
import com.szabh.smable3.entity.BleDeviceInfo.Companion.ANTI_LOST_HIDE
import com.szabh.smable3.entity.BleDeviceInfo.Companion.ANTI_LOST_VISIBLE
import com.szabh.smable3.entity.BleDeviceInfo.Companion.DIGITAL_POWER_HIDE
import com.szabh.smable3.entity.BleDeviceInfo.Companion.DIGITAL_POWER_VISIBLE
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_GOODIX
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_MTK
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_NORDIC
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_REALTEK
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PROTOTYPE_10G
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PROTOTYPE_R4
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PROTOTYPE_R5
import com.szabh.smable3.entity.BleDeviceInfo.Companion.WATCH_1
import com.szabh.smable3.entity.BleDeviceInfo.Companion.WATCH_0
import java.util.*

/**
 * 蓝牙设备相关信息，设备确认绑定后会返回该对象。
 */
data class BleDeviceInfo(
    var mId: Int = 0,
    /**
     * 设备支持读取的数据列表。
     */
    var mDataKeys: List<Int> = listOf(),

    /**
     * 设备蓝牙名。
     */
    var mBleName: String = "",

    /**
     * 设备蓝牙4.0地址。
     */
    var mBleAddress: String = "",

    /**
     * 芯片平台，[PLATFORM_NORDIC]、[PLATFORM_REALTEK]、[PLATFORM_MTK]或[PLATFORM_GOODIX]。
     */
    var mPlatform: String = "",

    /**
     * 设备原型，代表是基于哪款设备开发，[PROTOTYPE_10G]、[PROTOTYPE_R4]或[PROTOTYPE_R5]等。
     */
    var mPrototype: String = "",

    /**
     * 固件标记，固件那边所说的制造商，但严格来说，制造商表述并不恰当，且避免与后台数据结构中的分销商语义冲突，
     * 因为其仅仅用来区分固件，所以命名为FirmwareFlag，与[mBleName]一起确定唯一固件。
     */
    var mFirmwareFlag: String = "",

    /**
     * aGps文件类型，不同读GPS芯片需要下载不同的aGps文件，[AGPS_EPO]、[AGPS_UBLOX]或[AGPS_AGNSS]等，
     * 如果为0，代表不支持GPS。
     */
    var mAGpsType: Int = 0,

    /**
     * 发送[BleCommand.IO]的Buffer大小，见[BleConnector.sendStream]。
     */
    var mIOBufferSize: Long = 0L,

    /**
     * 表盘类型，[WATCH_0]、[WATCH_1]或[WATCH_2]。
     */
    var mWatchFaceType: Int = 0,

    /**
     * 设备蓝牙3.0地址。
     */
    var mClassicAddress: String = "",

    /**
     * 是否显示数字电量  [DIGITAL_POWER_VISIBLE] [DIGITAL_POWER_HIDE]
     */
    var mHideDigitalPower: Int = 0,

    /**
     * 是否显示防丢开关  [ANTI_LOST_VISIBLE] [ANTI_LOST_HIDE]
     */
    var mShowAntiLostSwitch: Int = 0,

    /**
     * 支持的睡眠算法类型  [SUPPORT_NEW_SLEEP_ALGORITHM_0] [SUPPORT_NEW_SLEEP_ALGORITHM_1]
     */
    var mSleepAlgorithmType: Int = 0,

    /**
     * 是否支持日期格式设置 [SUPPORT_DATE_FORMAT_0] [SUPPORT_DATE_FORMAT_1]
     */
    var mSupportDateFormatSet: Int = 0,

    /**
     * 是否支持读取设备信息。在之前, APP只能在绑定设备时被动接收到设备信息, 导致如果固件升级时修改了设备信息，APP不重新绑定
     * 就获取不了新的设备信息。加上该标记后, APP读取固件版本时, 如果发现与之前的版本不一致, 就主动更新下设备信息
     */
    var mSupportReadDeviceInfo: Int = 0,

    /**
     * 是否支持温度单位设置 [SUPPORT_TEMPERATURE_UNIT_0] [SUPPORT_TEMPERATURE_UNIT_1]
     */
    var mSupportTemperatureUnitSet: Int = 0
) : BleReadable() {

    override fun decode() {
        super.decode()
        mId = readInt32()
        mDataKeys = readBytesUtil(0).toList().chunked(2)
            .map { ((it[0].toInt() and 0xff) shl 8) or (it[1].toInt() and 0xff) }
        mBleName = readStringUtil(0)
        mBleAddress = readStringUtil(0).toUpperCase(Locale.getDefault())
        mPlatform = readStringUtil(0)
        mPrototype = readStringUtil(0)
        mFirmwareFlag = readStringUtil(0)
        mAGpsType = readInt8().toInt()
        mIOBufferSize = readUInt16().toLong()
        mWatchFaceType = readInt8().toInt()
        mClassicAddress = readStringUtil(0).toUpperCase(Locale.getDefault())
        mHideDigitalPower = readInt8().toInt()
        mShowAntiLostSwitch = readInt8().toInt()
        mSleepAlgorithmType = readInt8().toInt()
        mSupportDateFormatSet = readInt8().toInt()
        mSupportReadDeviceInfo = readInt8().toInt()
        mSupportTemperatureUnitSet = readInt8().toInt()
    }

    override fun toString(): String {
        return "BleDeviceInfo(mId=${String.format("0x%08X", mId)}, mDataKeys=" +
            "${mDataKeys.map { BleKey.of(it) }}, mBleName='$mBleName', " +
            "mBleAddress='$mBleAddress', mPlatform='$mPlatform', mPrototype='$mPrototype', " +
            "mFirmwareFlag='$mFirmwareFlag', mAGpsType=$mAGpsType, mIOBufferSize=$mIOBufferSize, " +
            "mWatchFaceType=$mWatchFaceType, mClassicAddress='$mClassicAddress', mHideDigitalPower=$mHideDigitalPower, " +
            "mShowAntiLostSwitch=$mShowAntiLostSwitch, mSleepAlgorithmType=$mSleepAlgorithmType, " +
            "mSupportDateFormatSet=$mSupportDateFormatSet, mSupportReadDeviceInfo=$mSupportReadDeviceInfo, " +
            "mSupportTemperatureUnitSet=$mSupportTemperatureUnitSet" +
            ")"
    }

    companion object {
        const val PLATFORM_NORDIC = "Nordic"
        const val PLATFORM_REALTEK = "Realtek"
        const val PLATFORM_MTK = "MTK"
        const val PLATFORM_GOODIX = "Goodix" // 汇顶

        // Nordic
        const val PROTOTYPE_10G = "SMA-10G"
        const val PROTOTYPE_GTM5 = "SMA-GTM5"
        const val PROTOTYPE_F1N = "SMA-F1N"
        const val PROTOTYPE_ND09 = "SMA-ND09"
        const val PROTOTYPE_ND08 = "SMA-ND08"

        // Realtek
        const val PROTOTYPE_R4 = "SMA-R4"
        const val PROTOTYPE_R5 = "SMA-R5"
        const val PROTOTYPE_B5CRT = "SMA-B5CRT"
        const val PROTOTYPE_F1RT = "SMA-F1RT"
        const val PROTOTYPE_F2 = "SMA-F2"
        const val PROTOTYPE_F3C = "SMA-F3C"
        const val PROTOTYPE_F3R = "SMA-F3R"
        const val PROTOTYPE_R7 = "SMA-R7"
        const val PROTOTYPE_F13 = "SMA-F13"
        const val PROTOTYPE_R10 = "R10"
        const val PROTOTYPE_F6 = "F6"
        const val PROTOTYPE_R9 = "R9"
        const val PROTOTYPE_F7 = "F7"
        const val PROTOTYPE_SW01 = "SMA-SW01"
        const val PROTOTYPE_REALTEK_GTM5 = "REALTEK_GTM5"
        const val PROTOTYPE_F1 = "SMA-F1"
        const val PROTOTYPE_F2D = "SMA-F2D"
        const val PROTOTYPE_F2R = "SMA-F2R"
        const val PROTOTYPE_T78 = "T78"
        const val PROTOTYPE_F5 = "F5"

        // Goodix
        const val PROTOTYPE_R3H = "R3H"
        const val PROTOTYPE_R3Q = "R3Q"

        // MTK
        const val PROTOTYPE_F3 = "F3"
        const val PROTOTYPE_M3 = "M3"
        const val PROTOTYPE_M4 = "M4"
        const val PROTOTYPE_M6 = "M6"
        const val PROTOTYPE_M7 = "M7"
        const val PROTOTYPE_R2 = "R2"
        const val PROTOTYPE_M6C = "M6C"
        const val PROTOTYPE_M7C = "M7C"
        const val PROTOTYPE_M7S = "M7S"
        const val PROTOTYPE_M4S = "M4S"
        const val PROTOTYPE_M4C = "M4C"
        const val PROTOTYPE_M5C = "M5C"

        const val AGPS_NONE = 0 // 无GPS芯片
        const val AGPS_EPO = 1 // MTK EPO
        const val AGPS_UBLOX = 2
        const val AGPS_AGNSS = 6 // 中科微

        const val WATCH_0 = 0           // 不支持表盘
        const val WATCH_1 = 1           // Q3表盘
        const val WATCH_2 = 2           //MTK标准化表盘
        const val WATCH_3 = 3           //Realtek bmp格式表盘 方形
        const val WATCH_4 = 4           //MTK-小尺寸表盘 要求表盘文件不超过40K
        const val WATCH_5 = 5           //MTK-表盘文件分辨率320x385
        const val WATCH_6 = 6           //MTK-表盘文件分辨率320x363
        const val WATCH_7 = 7           //Realtek bmp格式表盘 圆形
        const val WATCH_8 = 8           //汇顶平台表盘
        const val WATCH_9 = 9          //瑞昱R6,R8球拍屏，240x240
        const val WATCH_10 = 10        //瑞昱240*280方形表盘BMP格式（单蓝牙）（中间件项目，表盘需字节对齐）
        const val WATCH_11 = 11        //瑞昱bmp格式表盘, 圆形表盘  240*240，双模蓝牙
        const val WATCH_12 = 12        //瑞昱bmp格式表盘，方形表盘 240*240 双模蓝牙
        const val WATCH_13 = 13        //MTK 240x240-新表盘
        const val WATCH_14 = 14        //瑞昱80*160方形表盘BMP格式
        const val WATCH_15 = 15        //360x360 BMP 圆形-目前应用于瑞昱平台
        const val WATCH_16 = 16        //瑞昱240*280方形表盘BMP格式（双蓝牙）
        const val WATCH_17 = 17        //瑞昱 454x454 圆形 双蓝牙 R9 （中间件项目，表盘需字节对齐）
        const val WATCH_18 = 18        //瑞昱 240x240 圆形 单蓝牙 GTM5（中间件项目，表盘需字节对齐）
        const val WATCH_19 = 19        //瑞昱240*280方形表盘BMP格式（单蓝牙）
        const val WATCH_20 = 20        //瑞昱240*280方形表盘BMP格式（双蓝牙）
        const val WATCH_21 = 21        //瑞昱240*295方形表盘BMP格式（单蓝牙）（中间件项目，表盘需字节对齐）

        const val DIGITAL_POWER_VISIBLE = 0 //显示
        const val DIGITAL_POWER_HIDE = 1

        const val ANTI_LOST_VISIBLE = 1 //防丢显示
        const val ANTI_LOST_HIDE = 0    //防丢默认隐藏

        const val SUPPORT_NEW_SLEEP_ALGORITHM_0 = 0    //新版睡眠算法-不支持-默认
        const val SUPPORT_NEW_SLEEP_ALGORITHM_1 = 1   //新版睡眠算法

        const val SUPPORT_DATE_FORMAT_1 = 1   //支持
        const val SUPPORT_DATE_FORMAT_0 = 0   //默认不支持

        const val SUPPORT_READ_DEVICE_INFO_1 = 1   //支持
        const val SUPPORT_READ_DEVICE_INFO_0 = 0   //默认不支持

        const val SUPPORT_TEMPERATURE_UNIT_1 = 1   //支持
        const val SUPPORT_TEMPERATURE_UNIT_0 = 0   //默认不支持
    }
}
