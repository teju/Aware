package com.bestmafen.baseble.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.text.TextUtils
import com.bestmafen.baseble.util.BleLog
import java.util.*

/**
 * [AbsBleScanner]在API<21的实现类。
 */
internal class BleScanner18(
    serviceUuids: Array<UUID>? = null
) : AbsBleScanner(serviceUuids) {
    private var mLeScanCallback: BluetoothAdapter.LeScanCallback? = object : BluetoothAdapter.LeScanCallback {

        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            if (!isScanning) return
            if (device == null) return

            val name = device.name
            val address = device.address
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) return

            val bleDevice = BleDevice(
                mBluetoothDevice = device,
                mRssi = rssi,
                mScanRecord = scanRecord
            )
            if (mScanFilter == null || mScanFilter!!.match(bleDevice)) {
                BleLog.i("$LOG_HEADER onDeviceFound -> $bleDevice")
                mHandler.post { mBleScanCallback?.onDeviceFound(bleDevice) }
            }
        }
    }

    @Synchronized
    override fun scan(scan: Boolean) {
        requireNotNull(mBleScanCallback) { "BleScanCallback cannot be null" }
        BleLog.d("$LOG_HEADER scan $scan -> isScanning=$isScanning")
        if (isScanning == scan) return

        if (scan) {
            if (!mBluetoothAdapter.isEnabled) {
                mHandler.post { mBleScanCallback?.onBluetoothDisabled() }
                return
            }

            mBluetoothAdapter.startLeScan(mServiceUuids, mLeScanCallback)
            stopScanDelay()
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback)
            removeStop()
        }
        isScanning = scan
        mHandler.post { mBleScanCallback?.onScan(isScanning) }
    }

    override fun exit() {
        super.exit()
        mLeScanCallback = null
    }

    companion object {
        private const val LOG_HEADER = "BleScanner18"
    }
}
