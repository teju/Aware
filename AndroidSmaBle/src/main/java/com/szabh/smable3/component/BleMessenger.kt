package com.szabh.smable3.component

import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.bestmafen.baseble.messenger.*
import com.bestmafen.baseble.util.BleLog
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class BleMessenger : AbsBleMessenger() {
    private val mBleMessages = LinkedList<BleMessage>()
    private val mWritePackets = LinkedBlockingQueue<WriteMessage>()
    private val mPacketSemaphore = Semaphore(1)

    private var mHandler = Handler(Looper.getMainLooper())
    private var mMessageTask: MessageTask? = null

    var mMessengerCallback: BleMessengerCallback? = null

    init {
        thread(name = "$LOG_HEADER WritePacket") {
            while (true) {
                val packet = mWritePackets.take()
                mAbsBleConnector.mBluetoothGatt?.let { gatt ->
                    mAbsBleConnector.getCharacteristic(packet.mService, packet.mCharacteristic)?.let { characteristic ->
                        mPacketSemaphore.acquire()
                        characteristic.value = packet.mData
                        gatt.writeCharacteristic(characteristic)
                    }
                }
            }
        }
    }

    @Synchronized
    override fun enqueueMessage(message: BleMessage) {
        BleLog.v("$LOG_HEADER enqueueMessage -> $message")
        mBleMessages.addLast(message)
        if (mMessageTask == null) {
            dequeueMessage()
        }
    }

    @Synchronized
    override fun dequeueMessage() {
        mMessageTask?.let {
            mHandler.removeCallbacks(it)
            mMessageTask = null
        }

        if (mBleMessages.isNotEmpty()) {
            mMessageTask = MessageTask(mBleMessages.removeFirst()).also {
                mHandler.post(it)
            }
        } else {
            BleLog.v("$LOG_HEADER dequeueMessage -> No message right now")
        }
    }

    @Synchronized
    fun enqueueWritePackets(message: WriteMessage) {
        val count = if (message.mData.size % mPacketSize == 0)
            message.mData.size / mPacketSize
        else
            message.mData.size / mPacketSize + 1

        if (count == 1) {
            BleLog.v("$LOG_HEADER enqueueWritePackets -> $message")
            mWritePackets.put(message)
        } else {
            for (i in 0 until count) { // 拆分
                val data = (message.mData).copyOfRange(i * mPacketSize,
                    if (i == count - 1) message.mData.size else (i + 1) * mPacketSize)
                WriteMessage(message.mService, message.mCharacteristic, data).let {
                    BleLog.v("$LOG_HEADER enqueueWritePackets -> $it")
                    mWritePackets.put(it)
                }
            }
        }
    }

    @Synchronized
    override fun dequeueWritePacket() {
        BleLog.v("$LOG_HEADER dequeueWritePacket")
        if (mPacketSemaphore.availablePermits() == 0) {
            mPacketSemaphore.release()
        }
    }

    @Synchronized
    fun replyMessage(message: WriteMessage) {
        BleLog.v("$LOG_HEADER replyMessage -> $message")
        enqueueWritePackets(message)
    }

    @Synchronized
    override fun reset() {
        BleLog.v("$LOG_HEADER -> reset")
        mMessageTask?.let {
            mHandler.removeCallbacks(it)
        }
        mMessageTask = null
        mBleMessages.clear()
        mWritePackets.clear()
        if (mPacketSemaphore.availablePermits() == 0) {
            mPacketSemaphore.release()
        } else if (mPacketSemaphore.availablePermits() > 1) {
            mPacketSemaphore.acquire(mPacketSemaphore.availablePermits() - 1)
        }
    }

    inner class MessageTask(private val message: BleMessage) : Runnable {
        private var mRetry = 0

        override fun run() {
            val gatt = mAbsBleConnector.mBluetoothGatt ?: return

            if (mRetry == RETRY_MAX_TIMES) {
                dequeueMessage()
                return
            }

            mRetry++
            BleLog.v("MessageTask -> try($mRetry), $message")
            when (message) {
                is ReadMessage -> {
                    mAbsBleConnector.getCharacteristic(message.mService, message.mCharacteristic)?.let { characteristic ->
                        gatt.readCharacteristic(characteristic)
                    }
                }

                is WriteMessage -> {
                    if (mRetry > 1) {
                        mMessengerCallback?.onRetry()
                    }
                    enqueueWritePackets(message)
                    mHandler.postDelayed(this, TIMEOUT)
                }

                is NotifyMessage -> {
                    mAbsBleConnector.getCharacteristic(message.mService, message.mCharacteristic)?.let { characteristic ->
                        mAbsBleConnector.getNotifyDescriptor(characteristic)?.let { descriptor ->
                            descriptor.value =
                                if (message.mEnabled) {
                                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                } else {
                                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                                }
                            gatt.setCharacteristicNotification(characteristic, message.mEnabled)
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }

                is RequestMtuMessage -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        gatt.requestMtu(512)
                    } else {
                        dequeueMessage()
                    }
                }

                is RequestConnectionPriorityMessage -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAbsBleConnector.mBluetoothGatt?.requestConnectionPriority(message.mPriority)
                    }
                    // BluetoothGatt.requestConnectionPriority方法在BluetoothGattCallback中没有对应的回调函数,
                    // 所以让下一条消息直接出队
                    dequeueMessage()
                }
            }
        }
    }

    companion object {
        private const val LOG_HEADER = "BleMessenger"
        const val TIMEOUT = 8 * 1000L // 消息超时时间, 毫秒数, 超时后重发
        const val RETRY_MAX_TIMES = 3 // 消息超时后的重发次数
    }
}