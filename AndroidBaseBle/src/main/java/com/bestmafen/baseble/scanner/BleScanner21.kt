package com.bestmafen.baseble.scanner

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import android.text.TextUtils
import com.bestmafen.baseble.util.BleLog
import java.util.*

/**
 * [AbsBleScanner]在API>=21的实现类。
 */
@androidx.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class BleScanner21(
    serviceUuids: Array<UUID>? = null,
    scanMode: ScanMode = ScanMode.BALANCED
) : AbsBleScanner(serviceUuids, scanMode) {
    private var mScanFilters: MutableList<ScanFilter>? = null

    private val mScanSettingsBuilder = ScanSettings.Builder()
    private val mScanSettings
        get() = mScanSettingsBuilder.apply {
            when (mScanMode) {
                ScanMode.LOW_POWER -> setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                ScanMode.BALANCED -> setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                ScanMode.LOW_LATENCY -> setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            }
        }.build()

    private var mScanCallback: android.bluetooth.le.ScanCallback? =
        object : android.bluetooth.le.ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (!isScanning) return
                if (result == null) return
                val device = result.device ?: return

                val name = device.name
                val address = device.address
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) return

                val bleDevice = BleDevice(
                    mBluetoothDevice = device,
                    mRssi = result.rssi,
                    mScanRecord = result.scanRecord?.bytes
                )
                if (mScanFilter == null || mScanFilter!!.match(bleDevice)) {
                    BleLog.i("$LOG_HEADER onDeviceFound -> $bleDevice")
                    mHandler.post { mBleScanCallback?.onDeviceFound(bleDevice) }
                }
            }
        }

    init {
        if (mServiceUuids != null) {
            mScanFilters = mutableListOf()
            mServiceUuids.forEach { uuid ->
                mScanFilters?.add(ScanFilter.Builder().setServiceUuid(ParcelUuid(uuid)).build())
            }
        }
    }

    @Synchronized
    override fun scan(scan: Boolean) {
        requireNotNull(mBleScanCallback) { "BleScanCallback cannot be null" }
        BleLog.d("$LOG_HEADER scan $scan -> isScanning=$isScanning , scanMode=${mScanSettings.scanMode}")
        if (isScanning == scan) return

        val scanner = mBluetoothAdapter.bluetoothLeScanner
        if (scan) {
            if (!mBluetoothAdapter.isEnabled || scanner == null) {
                mHandler.post { mBleScanCallback?.onBluetoothDisabled() }
                return
            }

            scanner.startScan(mScanFilters, mScanSettings, mScanCallback)
            stopScanDelay()
        } else {
            scanner?.stopScan(mScanCallback)
            removeStop()
        }
        isScanning = scan
        mHandler.post { mBleScanCallback?.onScan(isScanning) }
    }

    override fun exit() {
        super.exit()
        mScanCallback = null
    }

    companion object {
        private const val LOG_HEADER = "BleScanner21"
    }
}
