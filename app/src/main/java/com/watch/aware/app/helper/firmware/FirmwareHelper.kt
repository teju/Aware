package com.szabh.androidblesdk3.firmware

import android.content.Context
import android.content.Intent
import com.abupdate.iot_libs.OtaAgentPolicy
import com.abupdate.iot_libs.info.CustomDeviceInfo
import com.szabh.androidblesdk3.firmware.g.FirmwareUpgradeGActivity
import com.szabh.androidblesdk3.firmware.m.FirmwareUpgradeMActivity
import com.szabh.androidblesdk3.firmware.n.FirmwareUpgradeNActivity
import com.szabh.androidblesdk3.firmware.r.OtaTargetSelectorR
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.entity.BleDeviceInfo
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_GOODIX
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_MTK
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_NORDIC
import com.szabh.smable3.entity.BleDeviceInfo.Companion.PLATFORM_REALTEK
import java.io.File

object FirmwareHelper {

    /**
     * 1. [BleDeviceInfo.PLATFORM_NORDIC]和[BleDeviceInfo.PLATFORM_GOODIX]的设备需要先发送OTA指令让其进入ota模式，
     *    进入成功后进入到升级界面。
     * 2. [BleDeviceInfo.PLATFORM_MTK]的设备需要先通过[BleConnector.SERVICE_MTK]和[BleConnector.CH_MTK_OTA_META]
     *    读取固件信息，设备返回之后进入到升级界面。
     * 3. 其他设备，直接进入到升级界面。
     */
    fun gotoOta(context: Context) {
        when (BleCache.mPlatform) {
            PLATFORM_NORDIC, PLATFORM_GOODIX ->
                BleConnector.sendData(BleKey.OTA, BleKeyFlag.UPDATE)
            PLATFORM_MTK ->
                BleConnector.read(BleConnector.SERVICE_MTK, BleConnector.CH_MTK_OTA_META)
            else -> gotoOtaReally(context)
        }
    }

    fun gotoOtaReally(context: Context) {
        when (BleCache.mPlatform) {
            PLATFORM_NORDIC ->
                context.startActivity(Intent(context, FirmwareUpgradeNActivity::class.java))
            PLATFORM_REALTEK ->
                context.startActivity(Intent(context, OtaTargetSelectorR::class.java))
            PLATFORM_MTK ->
                context.startActivity(Intent(context, FirmwareUpgradeMActivity::class.java))
            PLATFORM_GOODIX ->
                context.startActivity(Intent(context, FirmwareUpgradeGActivity::class.java))
        }
    }
}