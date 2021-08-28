package com.watch.aware.app

import android.app.Application
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import com.franmontiel.localechanger.LocaleChanger

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