package com.bestmafen.baseble.messenger

import android.bluetooth.BluetoothGatt
import com.bestmafen.baseble.data.mHexString

/**
 * 发送端的消息对象，代表了手机对蓝牙设备发起的一次操作。
 */
open class BleMessage

/**
 * 写操作，相当于执行n次[BluetoothGatt.writeCharacteristic]，如果消息长度超过了[AbsBleMessenger.mPacketSize]，
 * 会被拆分成多条消息。
 */
class WriteMessage(
    val mService: String,
    val mCharacteristic: String,
    val mData: ByteArray
) : BleMessage() {

    override fun toString(): String {
        return "WriteMessage(service='$mService', characteristic='$mCharacteristic', data=${mData.mHexString}"
    }
}

/**
 * 读操作，相当于执行[BluetoothGatt.readCharacteristic]。
 */
data class ReadMessage(
    val mService: String,
    val mCharacteristic: String
) : BleMessage()

/**
 * 打开通知，相当于执行[BluetoothGatt.setCharacteristicNotification]和[BluetoothGatt.writeDescriptor]。
 */
data class NotifyMessage(
    val mService: String,
    val mCharacteristic: String,
    val mEnabled: Boolean = true
) : BleMessage()

/**
 * 请求MTU，相当于执行[BluetoothGatt.requestMtu]，设备返回MTU后，会将其赋值给[AbsBleMessenger.mPacketSize]。
 */
data class RequestMtuMessage(
    val mMtu: Int = 512
) : BleMessage()

/**
 * 请求连接优先级，相当于执行[BluetoothGatt.requestConnectionPriority]，会调整与设备的通讯速率，但是可能不会立即生效，
 * 也不会一直有效。
 */
data class RequestConnectionPriorityMessage(
    /**
     * [BluetoothGatt.CONNECTION_PRIORITY_BALANCED] or
     * [BluetoothGatt.CONNECTION_PRIORITY_HIGH] or
     * [BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER]
     */
    val mPriority: Int
) : BleMessage()