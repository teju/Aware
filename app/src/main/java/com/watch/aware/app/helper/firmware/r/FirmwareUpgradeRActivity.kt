package com.szabh.androidblesdk3.firmware.r

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.realsil.sdk.dfu.DfuConstants
import com.realsil.sdk.dfu.model.DfuConfig
import com.realsil.sdk.dfu.model.DfuProgressInfo
import com.realsil.sdk.dfu.model.Throughput
import com.realsil.sdk.dfu.utils.DfuAdapter
import com.realsil.sdk.dfu.utils.DfuHelper
import com.realsil.sdk.dfu.utils.GattDfuAdapter
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.watch.aware.app.R

/**
 * Realtek设备固件升级
 */
@SuppressLint("SetTextI18n")
class FirmwareUpgradeRActivity : AppCompatActivity() {
    private val textView by lazy { findViewById<TextView>(R.id.text_view) }
    private val btn by lazy { findViewById<Button>(R.id.btn) }

    private val mDfuHelper by lazy { GattDfuAdapter.getInstance(this) }
    private val mDfuHelperCallback = object : DfuAdapter.DfuHelperCallback() {

        override fun onStateChanged(state: Int) {
            runOnUiThread {
                when (state) {
                    DfuAdapter.STATE_INIT_OK -> textView.text = "STATE_INIT_OK"
                    DfuAdapter.STATE_PREPARED -> textView.text = "STATE_PREPARED"
//                    DfuAdapter.STATE_CONNECTING -> textView.text = "STATE_CONNECTING"
                    DfuAdapter.STATE_OTA_PROCESSING -> textView.text = "STATE_OTA_PROCESSING"
                    DfuAdapter.STATE_DISCONNECTED -> textView.text = "STATE_DISCONNECTED"
                    DfuAdapter.STATE_CONNECT_FAILED -> textView.text = "STATE_CONNECT_FAILED"
                    DfuAdapter.STATE_ABORTED -> textView.text = "STATE_ABORTED"
                    else -> {
                    }
                }
            }
        }

        override fun onError(type: Int, code: Int) {
        }

        override fun onProcessStateChanged(state: Int, throughput: Throughput?) {
            if (state == DfuConstants.PROGRESS_IMAGE_ACTIVE_SUCCESS) finish()
        }

        override fun onProgressChanged(progressInfo: DfuProgressInfo) {
            runOnUiThread {
                textView.text = "$progressInfo"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firmware_upgrade)
        BleConnector.closeConnection(true)
        mDfuHelper.initialize(mDfuHelperCallback)
    }

    override fun onDestroy() {
        mDfuHelper.run {
            abort()
            close()
        }
        BleConnector.launch()
        super.onDestroy()
    }

    fun upgrade(view: View) {
        btn.isEnabled = false
        DfuConfig().apply {
            address = BleCache.mDfuAddress
            otaWorkMode = DfuConstants.OTA_MODE_NORMAL_FUNCTION
            isAutomaticActiveEnabled = true
            isVersionCheckEnabled = false   //关闭版本校验
//            filePath = "sma_r5_0025.bin"
//            filePath = "sma_f2_SmartWatch101_0.0.2.6.bin"
//            filePath = "sma_f2_SmartWatch101_0.0.2.7.bin"
            filePath = "sma_f2_SmartWatch101_0.0.3.4.bin"
            fileLocation = DfuConfig.FILE_LOCATION_ASSETS
        }.let {
            mDfuHelper.startOtaProcess(it)
        }
    }
}