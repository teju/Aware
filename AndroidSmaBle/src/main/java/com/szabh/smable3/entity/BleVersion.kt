package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

// 版本号分隔符之间的数字不允许超过9，因为：
// 1. 在服务器上，版本是是采用字符串保存的，数字超过9会导致版本比较错误
// 2. R5的在刷UI包时出错，会导致版本号变成FF，如果不处理，查询新版本时会出问题
data class BleVersion(var mVersion: String = "") : BleReadable() {

    override fun decode() {
        super.decode()
        mVersion = mBytes?.joinToString(".") {
            val v = it.toInt().and(0xff)
            if (v > 9) "0" else "$v"
        } ?: ""
    }
}
