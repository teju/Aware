package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

data class BleRealtimeLog(var mContent: String = "") : BleReadable() {

    override fun decode() {
        super.decode()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS    ", Locale.getDefault())
        mContent = if (mBytes != null)
            dateFormat.format(Date()) + String(mBytes!!, StandardCharsets.UTF_8)
        else
            dateFormat.format(Date()) + "null"
    }
}
