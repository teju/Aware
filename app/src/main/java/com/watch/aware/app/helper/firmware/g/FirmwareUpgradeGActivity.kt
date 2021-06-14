package com.szabh.androidblesdk3.firmware.g

import android.annotation.SuppressLint
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ZipUtils
import com.goodix.ble.gr.libdfu.EasyDfu.DfuEvent
import com.goodix.ble.libcomx.ILogger

import com.szabh.androiddfu.goodix.DfuConnection
import com.szabh.smable3.BleKey
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.watch.aware.app.R
import com.watch.aware.app.helper.REQUEST_CODE_G_FIRMWARE
import com.watch.aware.app.helper.SDK_FILE_ROOT
import com.watch.aware.app.helper.chooseFile
import java.io.File
import java.io.FileInputStream
import kotlin.properties.Delegates

/**
 * Goodix汇顶设备固件升级，升级之前需要发送OTA指令，让设备进入OTA模式。
 */
@SuppressLint("SetTextI18n")
class FirmwareUpgradeGActivity : AppCompatActivity(), Observer<Any>, DfuEvent, ILogger {
    private val textView by lazy { findViewById<TextView>(R.id.text_view) }
    private val btn by lazy { findViewById<Button>(R.id.btn) }

    private var mText by Delegates.observable("", { _, _, text ->
        runOnUiThread {
            textView.text = text
        }
    })

    private val mContext: FirmwareUpgradeGActivity by lazy { this }
    private var dfuConnection: DfuConnection? = null
    private val mDevice by lazy { BluetoothAdapter.getDefaultAdapter().getRemoteDevice(BleCache.mBleAddress) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firmware_upgrade)
        BleConnector.closeConnection(true)
    }

    override fun onDestroy() {
        BleConnector.launch()
        resetDfuConnection()
        dfuConnection?.requestDisconnect()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ListActivity.RESULT_OK) {
            val uri = data?.data ?: return

            //TODO 下面的代码会有问题, 需要重新实现怎么从uri中获取文件path
            val zipFilePath = uri.toFile().absolutePath
            val files = ZipUtils.unzipFile(zipFilePath, SDK_FILE_ROOT).filter {
                it.isFile && it.path.endsWith(".bin")
            }
            if (files.size == 2)
                dfuConnection?.dfu?.startDfuInABMode(FileInputStream(files[0]), FileInputStream(files[1]))
        }
    }

    fun upgrade(view: View) {
        btn.isEnabled = false
        btn.postDelayed({
            resetDfuConnection()
            dfuConnection = DfuConnection(mContext, mDevice).apply {
                connectionState.observe(mContext, mContext)
                errorInfo.observe(mContext, mContext)
                dfu.setListener(mContext)
                dfu.setLogger(mContext)
                requestConnect()
            }
        }, 4000)
    }

    private fun resetDfuConnection() {
        dfuConnection?.run {
            connectionState.removeObserver(mContext)
            errorInfo.removeObserver(mContext)
            dfu.setListener(null)
            dfu.setLogger(null)
        }
    }

    override fun onChanged(connState: Any) {
        mText = "onChanged: $connState"
        if (connState is DfuConnection.ConnState) {
            when (connState) {
                DfuConnection.ConnState.DISCONNECTED -> {
                    dfuConnection = null
                    btn.isEnabled = true
                }
                DfuConnection.ConnState.READY -> {
                    // 通过两个文件升级
//                    dfuConnection?.dfu?.startDfuInABMode(resources.openRawResource(R.raw.r3h_fw_022_a),
//                        resources.openRawResource(R.raw.r3h_fw_012_b))

                    // 选择zip文件升级，需要先把两个固件文件压缩成zip文件。
                    chooseFile(this, REQUEST_CODE_G_FIRMWARE)
                }
                else -> {
                }
            }
        }
    }

    override fun onDfuStart() {
        mText = "onDfuStart"
    }

    override fun onDfuProgress(percent: Int) {
        mText = "onDfuProgress: $percent%"
    }

    override fun onDfuComplete() {
        finish()
    }

    override fun onDfuErrorFirmwareOverlay() {
        mText = "onDfuErrorFirmwareOverlay"
        btn.isEnabled = true
    }

    override fun onDfuError(p0: String, p1: Error) {
        mText = "onDfuError: $p0, $p1"
        btn.isEnabled = true
    }

    override fun v(p0: String?, p1: String) {
        Log.v(p0, p1)
    }

    override fun d(p0: String?, p1: String) {
        Log.d(p0, p1)
    }

    override fun i(p0: String?, p1: String) {
        Log.i(p0, p1)
    }
    override fun w(p0: String?, p1: String) {
        Log.w(p0, p1)
    }

    override fun e(p0: String?, p1: String) {
        Log.e(p0, p1)
    }

    override fun e(p0: String?, p1: String, p2: Throwable?) {
        Log.e(p0, "$p1, $p2")
    }
}