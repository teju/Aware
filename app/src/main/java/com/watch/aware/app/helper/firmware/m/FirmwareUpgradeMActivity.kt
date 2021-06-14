package com.szabh.androidblesdk3.firmware.m

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abupdate.iot_libs.OtaAgentPolicy
import com.abupdate.iot_libs.info.VersionInfo
import com.abupdate.iot_libs.inter.ICheckVersionCallback
import com.abupdate.iot_libs.inter.IDownloadListener
import com.szabh.androiddfu.mtk.MtkOtaHelper
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.watch.aware.app.R
import java.io.File
import kotlin.properties.Delegates

/**
 * MTK设备固件升级，升级之前需要读取固件信息。
 */
@SuppressLint("SetTextI18n")
class FirmwareUpgradeMActivity : AppCompatActivity() {
    private val textView by lazy { findViewById<TextView>(R.id.text_view) }
    private val btn by lazy { findViewById<Button>(R.id.btn) }

    private var mState: State by Delegates.observable(State.NONE) { _, _, state ->
        when (state) {
            State.PREPARE_COMPLETED -> {
                textView.text = BleCache.getMtkOtaMeta().replace(";", "\n\n")
                btn.isEnabled = true
                btn.text = "Check Version"
            }
            State.PREPARE_FAILED -> {
                textView.text = BleCache.getMtkOtaMeta().replace(";", "\n\n")
                btn.isEnabled = false
                btn.text = "$state"
            }
            State.CHECKING_VERSION -> {
                btn.isEnabled = false
                btn.text = "$mState"
            }
            State.NEW_VERSION_FOUND -> {
                textView.text = "${OtaAgentPolicy.getVersionInfo()}"
                btn.isEnabled = true
                btn.text = "Download"
            }
            State.LATEST_VERSION -> {
                textView.text = "$mState"
                btn.isEnabled = true
                btn.text = "Check"
            }
            State.DOWNLOADING -> {
                textView.text = "$mState"
                btn.isEnabled = true
                btn.text = "Cancel downloading"
            }
            State.DOWNLOAD_FAILED -> {
                textView.text = "$mState"
                btn.isEnabled = true
                btn.text = "Download again"
            }
            State.DOWNLOAD_COMPLETED -> {
                textView.text = "$mState"
                btn.isEnabled = true
                btn.text = "Upgrade"
            }
            State.DOWNLOAD_CANCELED -> {
                textView.text = "$mState"
                btn.isEnabled = true
                btn.text = "Download again"
            }
            State.UPGRADING -> {
                btn.isEnabled = false
                btn.text = "Upgrading"
            }
            else -> {
            }
        }
    }
    private val mPath by lazy { File(getExternalFilesDir(null), "/adupsfota/update.zip").absolutePath }

    private val mBleHandleCallback = object : BleHandleCallback {

        override fun onStreamProgress(status: Boolean, errorCode: Int, total: Int, completed: Int) {
            textView.text = "Upgrading: $completed/$total"
            if (status && total == completed) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firmware_upgrade)
        BleConnector.addHandleCallback(mBleHandleCallback)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mState =
            if (MtkOtaHelper.prepare(BleCache.getMtkOtaMeta(), mPath))
                State.PREPARE_COMPLETED
            else
                State.PREPARE_FAILED
    }

    override fun onDestroy() {
        BleConnector.removeHandleCallback(mBleHandleCallback)
        super.onDestroy()
    }

    private fun checkVersion() {
        mState = State.CHECKING_VERSION
        OtaAgentPolicy.checkVersionAsync(object : ICheckVersionCallback {

            /*VersionInfo{
                versionName='POWER-G1_COA_V1.0.4_20200615-1508'
                versionAlias='POWER-G1_COA_V1.0.4_20200615-1508'
                fileSize=18328
                deltaID='3892311'
                md5sum='261d72ce3b89cd9acf9880bb2ec79b96'
                deltaUrl='http://iotdown.mayitek.com/1558436483/3892311/4442f5ef-17ce-45bf-a75e-ba4fe2d6e317.zip'
                publishDate='2020-06-15'
                content='[{"country":"zh_CN","content":"1.优化系统.修复错误"}]'
            }*/
            override fun onCheckSuccess(versionInfo: VersionInfo?) {
                mState = State.NEW_VERSION_FOUND
            }

            override fun onCheckFail(status: Int) {
                mState = State.LATEST_VERSION
            }
        })
    }

    private fun download() {
        OtaAgentPolicy.downloadAsync(object : IDownloadListener {

            override fun onPrepare() {
                mState = State.DOWNLOADING
            }

            override fun onDownloadProgress(downSize: Long, totalSize: Long) {
                textView.text = "Downloading: $downSize/$totalSize"
            }

            override fun onFailed(error: Int) {
                mState = State.DOWNLOAD_FAILED
            }

            override fun onCompleted() {
                mState = State.DOWNLOAD_COMPLETED
            }

            override fun onCancel() {
                mState = State.DOWNLOAD_CANCELED
            }
        })
    }

    private fun upgrade() {
        mState = State.UPGRADING
        BleConnector.mtkOta(OtaAgentPolicy.getConfig().updatePath)
    }

    fun upgrade(view: View) {
        when (mState) {
            State.PREPARE_COMPLETED, State.LATEST_VERSION -> checkVersion()
            State.NEW_VERSION_FOUND, State.DOWNLOAD_FAILED, State.DOWNLOAD_CANCELED -> download()
            State.DOWNLOADING -> OtaAgentPolicy.downloadCancel()
            State.DOWNLOAD_COMPLETED -> upgrade()
            else -> {
            }
        }
    }
}

internal enum class State {
    NONE, PREPARE_COMPLETED, PREPARE_FAILED, CHECKING_VERSION, NEW_VERSION_FOUND, LATEST_VERSION,
    DOWNLOADING, DOWNLOAD_FAILED, DOWNLOAD_COMPLETED, DOWNLOAD_CANCELED, UPGRADING
}