package com.szabh.smable3.component

const val BLE_OK: Byte = 0
const val BLE_ERROR: Byte = 1

const val ID_ALL = 0xff
const val DATA_EPOCH = 946684800 // 1970/1/1 00:00:00距离2000/1/1 00:00:00的秒数

object BleState {
    /**
     * 设备已断开
     */
    const val DISCONNECTED = -1

    /**
     * 设备已连接，但还未执行发现服务，通知矢能，设置MTU，还不能收发指令
     */
    const val CONNECTED = 0

    /**
     * 设备已就绪，已执行发现服务，通知矢能，设置MTU，可以正常收发发送指令
     */
    const val READY = 1
}

object CameraState {
    const val EXIT = 0
    const val ENTER = 1
    const val CAPTURE = 2

    fun getState(state: Int) = when (state) {
        EXIT -> "Exit"
        ENTER -> "Enter"
        CAPTURE -> "Capture"
        else -> "Unknown"
    }
}

object SyncState {
    const val DISCONNECTED = -2 // 同步过程中，连接断开
    const val TIMEOUT = -1 // 同步超时
    const val COMPLETED = 0 // 同步完成
    const val SYNCING = 1 // 同步中

    fun getState(state: Int) = when (state) {
        DISCONNECTED -> "Disconnected"
        TIMEOUT -> "Timeout"
        COMPLETED -> "completed"
        SYNCING -> "Syncing"
        else -> "Unknown"
    }
}

object WorkoutState {
    const val START = 1
    const val ONGOING = 2
    const val PAUSE = 3
    const val END = 4

    fun getState(state: Int) = when (state) {
        START -> "Start"
        ONGOING -> "Ongoing"
        PAUSE -> "Pause"
        END -> "End"
        else -> "Unknown"
    }
}