package com.szabh.androidblesdk3.tools

import com.blankj.utilcode.util.PermissionUtils

fun PermissionUtils.require(vararg permission: String, action: (Boolean) -> Unit) {
    if (PermissionUtils.isGranted(*permission)) {
        action(true)
    } else {
        callback(object : PermissionUtils.SimpleCallback {

            override fun onGranted() {
                action(true)
            }

            override fun onDenied() {
                action(false)
            }
        }).request()
    }
}