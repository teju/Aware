package com.watch.aware.app

import android.app.Application
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import com.blankj.utilcode.util.Utils
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.BleTime
import com.szabh.smable3.entity.BleTimeZone
import com.szabh.smable3.entity.Languages

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        val connector = BleConnector.Builder(this)
            .supportRealtekDfu(true) // 是否支持Realtek设备Dfu，如果不需要支持传false。
            .supportMtkOta(true) // 是否支持MTK设备Ota，如果不需要支持传false。
            .supportLauncher(true) // 是否支持自动连接Ble蓝牙设备方法（如果绑定的话），如果不需要请传false
            .supportFilterEmpty(true) // 是否支持过滤空数据，如ACTIVITY、HEART_RATE、BLOOD_PRESSURE、SLEEP、WORKOUT、LOCATION、TEMPERATURE、BLOOD_OXYGEN、HRV，如果不需要支持传false。
            .build()

        connector.addHandleCallback(object : BleHandleCallback {

            override fun onSessionStateChange(status: Boolean) {
                if (status) {
                    connector.sendObject(BleKey.TIME_ZONE, BleKeyFlag.UPDATE, BleTimeZone())
                    connector.sendObject(BleKey.TIME, BleKeyFlag.UPDATE, BleTime.local())
                    connector.sendInt8(BleKey.HOUR_SYSTEM, BleKeyFlag.UPDATE,
                        if (DateFormat.is24HourFormat(Utils.getApp())) 0 else 1)
                    connector.sendData(BleKey.POWER, BleKeyFlag.READ)
                    connector.sendData(BleKey.FIRMWARE_VERSION, BleKeyFlag.READ)
                    connector.sendInt8(BleKey.LANGUAGE, BleKeyFlag.UPDATE, Languages.languageToCode())
                    connector.sendData(BleKey.MUSIC_CONTROL, BleKeyFlag.READ)
                    connector.sendData(BleKey.STEP_GOAL, BleKeyFlag.READ)
                    connector.sendData(BleKey.ACTIVITY, BleKeyFlag.READ)
                    connector.sendData(BleKey.ACTIVITY_REALTIME, BleKeyFlag.READ)
                    connector.sendData(BleKey.DATA_ALL, BleKeyFlag.READ)
                }
            }
        })
    }
}