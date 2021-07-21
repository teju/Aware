package com.bestmafen.baseble.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import com.bestmafen.baseble.util.BleLog
import java.util.*

/**
 * 扫描器对象的基类，用于屏蔽不同API版本下不同的扫描行为：
 * <21，[BluetoothAdapter.startLeScan]
 * >=21，[BluetoothLeScanner.startScan]
 *
 * This is an abstract class which is used to replace [BluetoothAdapter.startLeScan] and
 * [BluetoothLeScanner.startScan] at different API level.
 */
abstract class AbsBleScanner(
    /**
     * 扫描包含指定服务的设备，如果为null，则扫描所有设备。
     */
    val mServiceUuids: Array<UUID>? = null,

    /**
     * 扫描模式，不同的扫描模式会有不同的扫描频率和功耗，API>=21时有效。
     */
    var mScanMode: ScanMode = ScanMode.BALANCED
) {
    protected val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    /**
     * 在主线程派发扫描相关事件.
     */
    protected val mHandler = Handler(Looper.getMainLooper())

    /**
     * 是否正在扫描。
     */
    @Volatile
    var isScanning = false

    protected var mBleScanCallback: BleScanCallback? = null
    protected var mScanFilter: BleScanFilter? = null

    /**
     * 扫描持续时间，每次扫描在经历该时间后会自动停止
     */
    private var mScanDuration = DEFAULT_DURATION
    private val mStopScanning = Runnable { scan(false) }

    /**
     * 开启或停止扫描
     */
    abstract fun scan(scan: Boolean)

    /**
     * 设置扫描时间
     */
    fun setScanDuration(duration: Int): AbsBleScanner {
        mScanDuration = duration
        return this
    }

    fun setBleScanCallback(callback: BleScanCallback): AbsBleScanner {
        mBleScanCallback = callback
        return this
    }

    fun setScanFilter(filter: BleScanFilter): AbsBleScanner {
        mScanFilter = filter
        return this
    }

    /**
     * 停止并退出扫描，并移除扫描的回调接口，退出后不能再重新开启扫描。
     */
    open fun exit() {
        scan(false)
        mBleScanCallback = null
    }

    protected fun stopScanDelay() {
        BleLog.d("AbsBleScanner -> stopScanDelay")
        mHandler.postDelayed(mStopScanning, (mScanDuration * 1000).toLong())
    }

    protected fun removeStop() {
        BleLog.d("AbsBleScanner -> removeStop")
        mHandler.removeCallbacks(mStopScanning)
    }

    companion object {
        const val DEFAULT_DURATION = 10
    }
}

/**
 * 扫描模式，详细说明请参考[ScanSettings]类中相关常量
 */
enum class ScanMode {
    /**
     * 低功耗：扫描1秒，停5秒
     */
    LOW_POWER,

    /**
     * 均衡：扫描2秒，停3秒
     */
    BALANCED,

    /**
     * 低延迟：一直扫描
     */
    LOW_LATENCY
}
