package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

data class BleBleAddress(var mAddress: String = "") : BleReadable() {

    override fun decode() {
        super.decode()
        if (mBytes == null || mBytes!!.size < 6) return

        mAddress = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
            readInt8(), readInt8(), readInt8(), readInt8(), readInt8(), readInt8())
    }
}
