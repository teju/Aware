package com.szabh.androidblesdk3.firmware.n

import android.app.Activity
import com.watch.aware.app.BuildConfig
import no.nordicsemi.android.dfu.DfuBaseService

class DfuService : DfuBaseService() {

    override fun getNotificationTarget(): Class<out Activity> {
        return DfuNotificationActivity::class.java
    }

    override fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }
}