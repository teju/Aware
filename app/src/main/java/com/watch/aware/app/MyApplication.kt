package com.watch.aware.app

import android.app.Application
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import com.blankj.utilcode.util.Utils
import com.franmontiel.localechanger.LocaleChanger
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.BleTime
import com.szabh.smable3.entity.BleTimeZone
import com.szabh.smable3.entity.Languages
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LocaleChanger.initialize(applicationContext, SUPPORTED_LOCALES)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleChanger.onConfigurationChanged()
    }
    companion object {
        val SUPPORTED_LOCALES = Arrays.asList(
            Locale("en", "US"),
            Locale("cn", "CN")
        )
    }
}