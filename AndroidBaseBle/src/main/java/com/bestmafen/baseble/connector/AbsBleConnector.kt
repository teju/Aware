package com.bestmafen.baseble.connector

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.bestmafen.baseble.data.mHexString
import com.bestmafen.baseble.messenger.AbsBleMessenger
import com.bestmafen.baseble.messenger.NotifyMessage
import com.bestmafen.baseble.messenger.RequestMtuMessage
import com.bestmafen.baseble.parser.IBleParser
import com.bestmafen.baseble.scanner.*
import com.bestmafen.baseble.util.BleLog
import java.util.*

/**
 * [BluetoothGatt]的包装类，代表与一个蓝牙设备的连接。
 * 1.在指定连接目标后会一直重连，直到连接成功。
 * 2.重连时会采取通过地址直接连接和扫描两种方式交替执行，如果设备从来没有被扫描到过，就直接通过地址连的话，可能不会连接成功。
 * 3.重连间隔根据重试次数倍数回退。
 * 4.当检测到手机蓝牙开启时，如果之前有指定连接目标，会重新进行连接。
 * 5.连接成功后自动执行发现服务、开启通知和请求MTU。
 */
abstract class AbsBleConnector {
    var mBluetoothGatt: BluetoothGatt? = null
    private val mBluetoothGattCallback by lazy {
        object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                val success = status == BluetoothGatt.GATT_SUCCESS
                val connected = newState == BluetoothProfile.STATE_CONNECTED
                val connectionState = success && connected
                BleLog.i("$LOG_HEADER onConnectionStateChange -> $connectionState" +
                    ", status=${getGattStatus(status)}, newState=${getBluetoothProfileState(newState)}")
                mBleMessenger.reset()
                mBleParser.reset()
                if (connectionState) {
                    gatt.discoverServices()
                    connect(false)
                    mBleGattCallback?.onConnectionStateChange(true)
                } else {
                    if (mNotified) {
                        mBleGattCallback?.onConnectionStateChange(false)
                    }
                    connect(true)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                BleLog.i("$LOG_HEADER -> onServicesDiscovered")
                mBleMessenger.enqueueMessage(NotifyMessage(mService, mNotify))
                mBleMessenger.enqueueMessage(RequestMtuMessage())
            }

            override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
                BleLog.i("$LOG_HEADER -> onDescriptorWrite")
                mNotified = true
                mBleMessenger.dequeueMessage()
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                BleLog.i("$LOG_HEADER onMtuChanged -> mtu=$mtu")
                // 某些设备一连上，还没发送mtu请求，就主动触发了该回调，如果是这种情况，直接忽略
                if (!mNotified) return

                mBleMessenger.mPacketSize = mtu - 3
                mBleMessenger.dequeueMessage()
                mBleGattCallback?.onMtuChanged()
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                val bytes = characteristic.value
                val text = characteristic.getStringValue(0)
                BleLog.i("$LOG_HEADER onCharacteristicRead -> ${characteristic.uuid}, ${bytes.mHexString}, $text")
                mBleMessenger.dequeueMessage()
                mBleGattCallback?.onCharacteristicRead("${characteristic.uuid}", bytes, text)
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                BleLog.i("$LOG_HEADER onCharacteristicWrite -> ${getGattStatus(status)}, ${characteristic.uuid}" +
                    ", ${characteristic.value.mHexString}")
                mBleMessenger.dequeueWritePacket()
                mBleGattCallback?.onCharacteristicWrite("${characteristic.uuid}", characteristic.value)
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                BleLog.i("$LOG_HEADER onCharacteristicChanged -> ${characteristic.uuid}, ${characteristic.value.mHexString}")
                mBleParser.onReceive(characteristic.value)?.let {
                    mBleGattCallback?.onCharacteristicChanged(it)
                }
            }
        }
    }
    protected var mBleGattCallback: BleGattCallback? = null

    protected lateinit var mContext: Context

    /**
     * 监听手机蓝牙的状态，当手机蓝牙开启时，如果已指定连接目标，重新进行连接。
     */
    private val mReceiver by lazy {
        object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                if (TextUtils.equals(intent.action, BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    BleLog.i("$LOG_HEADER onReceive BluetoothAdapter.ACTION_STATE_CHANGED ->" +
                        " state=" + getBluetoothAdapterState(state))
                    if (state == BluetoothAdapter.STATE_ON) {
                        connect(true)
                    }
                }
            }
        }
    }
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    /**
     * 在主线程派发相关事件。
     */
    protected val mHandler = Handler(Looper.getMainLooper())

    /**
     * 连接目标地址。
     */
    private var mTargetAddress: String? = null

    /**
     * 因为重连时会采取通过地址直接连接和扫描两种方式交替执行，所以需要一个扫描器对象。
     */
    private var mScanner: AbsBleScanner? = null

    /**
     * 用于指定匹配规则为设备的地址是[mTargetAddress]。
     */
    private val mScanFilter = AddressFilter("")

    /**
     * 是否正在进行连接。
     */
    private var isConnecting = false

    /**
     * 下次连接是否是通知地址直接连接，否则的话通过扫描连接。
     */
    private var mConnectDirectly = true

    /**
     * 重连间隔会根据重试次数而倍数回退。
     */
    private var mRetry = 0

    /**
     * 重连时间间隔基数，秒。
     */
    protected val mReconnectBasePeriod = 8

    /**
     * 重连最大时间间隔，秒。如果超出该间隔，间隔会重置为[mReconnectBasePeriod]
     */
    protected val mReconnectMaxPeriod = 40
    protected val mScanMaxDuration = 12 // 扫描最长时间，避免重连间隔太长时，长时间扫描。

    /**
     * 重连任务对象
     */
    private val mReconnection = object : Runnable {

        override fun run() {
            closeConnection(false)
            if (!shouldReconnect()) {
                isConnecting = false
                return
            }

            mRetry++
            if (mRetry < 1) mRetry = 1

            var period = mRetry * mReconnectBasePeriod
            // 重连间隔回退至最大间隔，重置间隔
            if (period > mReconnectMaxPeriod) {
                mRetry = 1
                period = mRetry * mReconnectBasePeriod
            }

            // 华为手机只通过扫描连接，因为通过地址连接失败后，会导致再也连接不了了，测试机器荣耀9X。
            if (Build.MANUFACTURER.equals("HUAWEI", true) || !mConnectDirectly) {
                BleLog.d("$LOG_HEADER connect scan")
                mScanner?.run {
                    // 扫描时间为重连间隔的3/4，但是也不能超过最大扫描时间
                    var scanDuration = (period * 0.75f).toInt()
                    if (scanDuration > mScanMaxDuration) {
                        scanDuration = mScanMaxDuration
                    }
                    setScanDuration(scanDuration)
                    scan(true)
                }
            } else {
                BleLog.d("$LOG_HEADER connect directly")
//                mBluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    mBluetoothAdapter.getRemoteDevice(mTargetAddress)
//                        .connectGatt(mContext, false, mBluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
//                } else {
//                    mBluetoothAdapter.getRemoteDevice(mTargetAddress)
//                        .connectGatt(mContext, false, mBluetoothGattCallback)
//                }

                mBluetoothGatt = mBluetoothAdapter.getRemoteDevice(mTargetAddress)
                    .connectGatt(mContext, false, mBluetoothGattCallback)
            }
            mConnectDirectly = !mConnectDirectly
            mHandler.postDelayed(this, (period * 1000).toLong())
        }
    }

    // 该变量是为了处理，在某些情况下，连上设备之后，还没发现服务，就已经触发了onMtuChanged，导致的连接流程错误。
    private var mNotified = false

    /**
     * 服务的UUID
     */
    abstract val mService: String

    /**
     * 通知的UUID
     */
    abstract val mNotify: String

    abstract val mBleMessenger: AbsBleMessenger

    abstract val mBleParser: IBleParser

    fun init(context: Context, bleGattCallback: BleGattCallback): AbsBleConnector {
        mContext = context.applicationContext
        mBleGattCallback = bleGattCallback
//        mScanner = ScannerFactory.newInstance(arrayOf(UUID.fromString(mService)))
        mScanner = ScannerFactory.newInstance()
            .setScanFilter(mScanFilter)
            .setBleScanCallback(object : BleScanCallback {

                override fun onBluetoothDisabled() {
                }

                override fun onScan(scan: Boolean) {
                }

                override fun onDeviceFound(device: BleDevice) {
                    BleLog.d("$LOG_HEADER onDeviceFound -> $device")
                    mScanner?.scan(false)
                    if (mBluetoothGatt != null) return

                    mBluetoothGatt = device.mBluetoothDevice.connectGatt(mContext, false, mBluetoothGattCallback)
                }
            })
        mBleMessenger.mAbsBleConnector = this

        mContext.registerReceiver(mReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        return this
    }

    /**
     * 设置连接目标。
     */
    fun setAddress(address: String): AbsBleConnector {
        mTargetAddress = address
        mScanFilter.mAddress = address
        return this
    }

    /**
     * 设置连接目标。
     */
    fun setBluetoothDevice(bluetoothDevice: BluetoothDevice): AbsBleConnector {
        return setAddress(bluetoothDevice.address)
    }

    /**
     * 设置连接目标。
     */
    fun setBleDevice(bleDevice: BleDevice): AbsBleConnector {
        return setBluetoothDevice(bleDevice.mBluetoothDevice)
    }

    /**
     * 开始或停止连接。
     */
    @Synchronized
    fun connect(connect: Boolean) {
        if (isConnecting == connect) return

        BleLog.d("$LOG_HEADER connect -> $connect")
        isConnecting = connect
        mRetry = 0
        if (connect) {
            mHandler.post(mReconnection)
        } else {
            mScanner!!.scan(false)
            mHandler.removeCallbacks(mReconnection)
        }
    }

    /**
     * 关闭当前连接。
     * @param stopReconnecting 是否停止重连
     */
    @Synchronized
    fun closeConnection(stopReconnecting: Boolean) {
        BleLog.d("$LOG_HEADER closeConnection -> stopReconnecting = $stopReconnecting")
        mBleMessenger.reset()
        mBleParser.reset()
        if (mNotified) {
            mNotified = false
            if (stopReconnecting) {
                mBleGattCallback?.onConnectionStateChange(false)
            }
        }
        if (mBluetoothGatt != null) {
            // mBluetoothGatt.disconnect();
            // refreshDeviceCache();
            mBluetoothGatt!!.close()
            mBluetoothGatt = null
        }

        if (stopReconnecting) {
            mTargetAddress = ""
            connect(false)
        }
    }

    //    private void refreshDeviceCache() {
    //        try {
    //            final Method refresh = BluetoothGatt.class.getMethod("refresh");
    //            if (refresh != null) {
    //                final boolean success = (Boolean) refresh.invoke(mBluetoothGatt);
    //                L.i("$LOG_HEADER refreshDeviceCache -> " + success);
    //            }
    //        } catch (Exception e) {
    //            L.e("$LOG_HEADER refreshDeviceCache -> error = " + e.getMessage());
    //        }
    //    }

    /**
     * 是否有必要进行连接，只有在手机蓝牙已开启，并且已指定连接目标时才需要发起连接操作。
     */
    private fun shouldReconnect(): Boolean {
        val reconnect = mBluetoothAdapter.isEnabled && !TextUtils.isEmpty(mTargetAddress)
        BleLog.d("$LOG_HEADER shouldReconnect -> $reconnect" +
            ", BluetoothAdapter isEnabled: ${mBluetoothAdapter.isEnabled}" +
            ", mTargetAddress: $mTargetAddress")
        return reconnect
    }

    /**
     * 退出并关闭连接，一般用不到。
     */
    fun exit() {
        BleLog.d("$LOG_HEADER -> exit")
        mScanner!!.exit()
        closeConnection(true)
        mContext.unregisterReceiver(mReceiver)
    }

    /**
     * 根据服务的UUID和特征的UUID获取[BluetoothGattCharacteristic]。
     */
    fun getCharacteristic(serviceUuid: String, characteristicUuid: String): BluetoothGattCharacteristic? {
        if (mBluetoothGatt == null) return null
        if (TextUtils.isEmpty(serviceUuid) || TextUtils.isEmpty(characteristicUuid)) return null

        val service = mBluetoothGatt!!.getService(UUID.fromString(serviceUuid))
        if (service == null) {
            BleLog.w("$LOG_HEADER getCharacteristic -> service($serviceUuid)=null")
            return null
        }

        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuid))
        if (characteristic == null) {
            BleLog.w("$LOG_HEADER getCharacteristic -> service($serviceUuid), characteristic($characteristicUuid)=null")
            return null
        }

        return characteristic
    }

    /**
     * 获取[BluetoothGattCharacteristic]下用于打开通知的[BluetoothGattDescriptor]。
     */
    fun getNotifyDescriptor(characteristic: BluetoothGattCharacteristic?): BluetoothGattDescriptor? {
        if (characteristic == null) {
            BleLog.w("$LOG_HEADER getNotifyDescriptor -> characteristic=null")
            return null
        }

        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        if (descriptor == null) {
            BleLog.w("$LOG_HEADER getNotifyDescriptor -> descriptor=null")
            return null
        }

        return descriptor
    }

    companion object {
        private const val LOG_HEADER = "AbsBleConnector"

        /**
         * 将一些关键的GATT状态常量转换为易阅读的文本
         */
        fun getGattStatus(status: Int) =
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> "$status SUCCESS"
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> "$status READ_NOT_PERMITTED"
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "$status WRITE_NOT_PERMITTED"
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "$status INSUFFICIENT_AUTHENTICATION"
                BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "$status REQUEST_NOT_SUPPORTED"
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "$status INSUFFICIENT_ENCRYPTION"
                BluetoothGatt.GATT_INVALID_OFFSET -> "$status INVALID_OFFSET"
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "$status INVALID_ATTRIBUTE_LENGTH"
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> "$status CONNECTION_CONGESTED"
                BluetoothGatt.GATT_FAILURE -> "$status FAILURE"
                else -> "$status unknown"
            }

        /**
         * 将一些关键的设备连接状态常量转换为易阅读的文本
         */
        fun getBluetoothProfileState(state: Int) =
            when (state) {
                BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECTED"
                BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
                BluetoothProfile.STATE_CONNECTED -> "CONNECTED"
                BluetoothProfile.STATE_DISCONNECTING -> "DISCONNECTING"
                else -> "$state unknown"
            }

        /**
         * 将一些关键的蓝牙状态常量转换为易阅读的文本
         */
        fun getBluetoothAdapterState(state: Int) =
            when (state) {
                BluetoothAdapter.STATE_OFF -> "STATE_OFF"
                BluetoothAdapter.STATE_TURNING_ON -> "STATE_TURNING_ON"
                BluetoothAdapter.STATE_ON -> "STATE_ON"
                BluetoothAdapter.STATE_TURNING_OFF -> "STATE_TURNING_OFF"
                else -> "$state unknown"
            }
    }
}
