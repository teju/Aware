package com.szabh.androidblesdk3.firmware.n

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bestmafen.baseble.scanner.*

import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.watch.aware.app.R
import com.watch.aware.app.helper.REQUEST_CODE_N_FIRMWARE
import com.watch.aware.app.helper.chooseFile
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper

/**
 * Nordic设备固件升级，升级之前需要发送OTA指令，让设备进入OTA模式。
 */
class FirmwareUpgradeNActivity : AppCompatActivity() {
    private val textView by lazy { findViewById<TextView>(R.id.text_view) }
    private val btn by lazy { findViewById<Button>(R.id.btn) }

    private val mBleScanner: AbsBleScanner by lazy {
        ScannerFactory.newInstance()
            .setScanDuration(10)
            .setScanFilter(AddressFilter(BleCache.mDfuAddress))
            .setBleScanCallback(object : BleScanCallback {

                override fun onBluetoothDisabled() {
                    textView.setText(R.string.enable_bluetooth)
                }

                override fun onScan(scan: Boolean) {
                    btn.isEnabled = !scan
                }

                override fun onDeviceFound(device: BleDevice) {
                    mBleScanner.scan(false)
                    mAddress = device.mBluetoothDevice.address
                    mName = device.mBluetoothDevice.name
                    chooseFile(this@FirmwareUpgradeNActivity, REQUEST_CODE_N_FIRMWARE)
                }
            })
    }

    private val mDfuProgressListener = object : DfuProgressListenerAdapter() {

        override fun onDeviceConnecting(deviceAddress: String) {
            textView.setText(R.string.device_connecting)
        }

        override fun onDeviceConnected(deviceAddress: String) {
            textView.setText(R.string.device_connected)
        }

        override fun onDfuProcessStarting(deviceAddress: String) {
            textView.setText(R.string.dfu_starting)
        }

        override fun onDfuProcessStarted(deviceAddress: String) {
            textView.setText(R.string.dfu_started)
        }

        override fun onProgressChanged(deviceAddress: String, percent: Int, speed: Float, avgSpeed: Float,
                                       currentPart: Int, partsTotal: Int) {
            textView.text = getString(R.string.dfu_progress, percent)
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            textView.setText(R.string.device_disconnecting)
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            textView.setText(R.string.device_disconnected)
            btn.isEnabled = true
        }

        override fun onDfuCompleted(deviceAddress: String) {
            finish()
        }

        override fun onDfuAborted(deviceAddress: String) {
            btn.isEnabled = true
            textView.setText(R.string.dfu_aborted)
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            btn.isEnabled = true
            textView.text = getString(R.string.dfu_error, "error=$error, errorType=$errorType, message=$message")
        }
    }

    private var mAddress = ""
    private var mName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firmware_upgrade)
        BleConnector.closeConnection(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ListActivity.RESULT_OK) {
            val uri = data?.data ?: return

            val controller = DfuServiceInitiator(mAddress).let {
                it.setDeviceName(mName)
                it.setKeepBond(true)
                it.setDisableNotification(true)
                it.setNumberOfRetries(3)
                // TODO 这里不确定是否可行, 待验证
                it.setZip(uri) // uri, path, or rawResId
                it.start(this@FirmwareUpgradeNActivity, DfuService::class.java)
            }
            // You may use the controller to pause, resume or abort the DFU process.
        }
    }

    override fun onResume() {
        super.onResume()
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener)
    }

    override fun onPause() {
        super.onPause()
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener)
    }

    override fun onDestroy() {
        mBleScanner.exit()
        BleConnector.launch()
        super.onDestroy()
    }

    fun upgrade(view: View) {
        mBleScanner.scan(true)
        btn.isEnabled = false
    }
}