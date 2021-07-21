package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

data class BleLanguagePackVersion(
    var mVersion: String = "0.0.0",
    var mLanguageCode: Int = Languages.DEFAULT_CODE
) : BleReadable() {

    override fun decode() {
        super.decode()
        if (mBytes != null && mBytes!!.size == 4) {
            mVersion = readBytes(3).joinToString(".") {
                val v = it.toInt().and(0xff)
                if (v > 9) "0" else "$v"
            }
            mLanguageCode = readUInt8().toInt()
        }
    }
}
