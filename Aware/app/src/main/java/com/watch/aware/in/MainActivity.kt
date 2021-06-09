package com.watch.aware.`in`

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val connector = BleConnector.Builder(this)
                .supportRealtekDfu(true) // Whether support Realtek device Dfu, pass false if not.
                .supportMtkOta(true) // Whether support MTK device Ota, pass false if not.
                .supportLauncher(true) // Whether to support automatic connection to Ble Bluetooth device method (if bound), if not required, please pass false
                .supportFilterEmpty(false) // Whether to support filtering empty data, such as ACTIVITY, HEART_RATE, BLOOD_PRESSURE, SLEEP, WORKOUT, LOCATION, TEMPERATURE, BLOOD_OXYGEN, HRV, if you don’t need to support false.
                .build()
    }
}