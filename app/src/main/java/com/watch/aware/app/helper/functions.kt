package com.watch.aware.app.helper

import android.Manifest
import android.app.Activity
import android.app.ListActivity
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.PermissionUtils
import com.szabh.androidblesdk3.tools.require
import com.szabh.smable3.BleKey
import com.szabh.smable3.component.BleConnector
import java.io.File

val SDK_FILE_ROOT: String = PathUtils.getExternalAppFilesPath()
const val REQUEST_CODE_G_FIRMWARE = 1
const val REQUEST_CODE_N_FIRMWARE = 2

fun copyAssert(context: Context, callback: (() -> Unit)? = null) {
    FileUtils.createOrExistsDir(SDK_FILE_ROOT)
    PermissionUtils
        .permission(PermissionConstants.STORAGE)
        .require(Manifest.permission.WRITE_EXTERNAL_STORAGE) { granted ->
            if (granted) {
                context.assets.list("")?.filter {
                    it.endsWith(".alp") || it.endsWith(".bin") || it.endsWith(".dat") || it.endsWith(".zip")
                }?.let { assertFiles ->
                    for (assertFile in assertFiles) {
                        val copyToFile = File(SDK_FILE_ROOT, assertFile)
                        if (!copyToFile.exists()) {
                            copyToFile.writeBytes(context.assets.open(assertFile).readBytes())
                        }
                    }
                }
            }
            callback?.invoke()
        }
}

fun chooseFile(activity: Activity, requestCode: Int) {
    val chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }
    activity.startActivityForResult(Intent.createChooser(chooseFile, "Choose a file"), requestCode)
}

fun dispatchChooseFileActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == ListActivity.RESULT_OK) {
        val uri = data?.data ?: return

        context.contentResolver.openInputStream(uri)?.let {
            BleConnector.sendStream(BleKey.of(requestCode), it)
        }
    }
}

