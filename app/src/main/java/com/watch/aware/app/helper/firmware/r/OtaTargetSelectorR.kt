package com.szabh.androidblesdk3.firmware.r

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter

// 用来选择ota目标，固件或扩展包（UI和语言）
class OtaTargetSelectorR : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("FIRMWARE", "EXTRA PACK"))
        listView.setOnItemClickListener { _, _, position, _ ->
            val clz = if (position == 0) {
                FirmwareUpgradeRActivity::class.java
            } else {
                ExtraPackUpgradeRActivity::class.java
            }
            startActivity(Intent(this, clz))
        }
    }
}