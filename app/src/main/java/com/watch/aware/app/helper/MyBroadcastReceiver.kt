package com.watch.aware.app.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.watch.aware.app.fragments.settings.BaseFragment

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent) {
        if(BleConnector.isAvailable()) {
            if (BleCache.mDeviceInfo?.mBleName != null) {
                if (Constants.SPO2 != 0 && Constants.HR != 0 && Constants.Temp != 0.0) {
                    BaseFragment.postSaveDeviceDataViewModel?.loadData(
                        Constants.SPO2,
                        Constants.HR,
                        Constants.Temp,
                        Constants.COUGH,
                        UserInfoManager.getInstance(context).getEmail(),
                        Constants._activity, Helper.getCurrentDate().toString()
                    )
                }
                Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ, context!!)
            }
        }
    }
}