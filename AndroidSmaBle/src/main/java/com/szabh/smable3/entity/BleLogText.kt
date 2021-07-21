package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable
import java.nio.charset.StandardCharsets

data class BleLogText(
    var mContent: String = ""
) : BleReadable() {

    override fun decode() {
        super.decode()
        if (mBytes != null && mBytes!!.isNotEmpty()) {
            val end = mBytes!!.indexOf(0)
            mContent = if (end == -1) {
                String(mBytes!!, StandardCharsets.UTF_8)
            } else {
                String(mBytes!!, 0, end, StandardCharsets.UTF_8)
            }
        }
    }

    companion object {
        const val ITEM_LENGTH = 64
    }
}
