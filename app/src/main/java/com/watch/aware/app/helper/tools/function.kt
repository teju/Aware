package com.szabh.androidblesdk3.tools

import android.content.Context
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.szabh.smable3.component.BleConnector

fun doBle(context: Context, action: () -> Unit) {
    if (BleConnector.isAvailable()) {
        action()
    }
}

fun toast(context: Context, text: String) {
    ToastUtils.showLong(text)
}
