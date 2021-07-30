package com.szabh.smable3.component

import android.bluetooth.BluetoothDevice
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.entity.*

/**
 * 事件是在主线程派发，所以不要在回调中做耗时操作
 */
interface BleHandleCallback {

    /**
     * 设备连接成功时触发。
     */
    fun onDeviceConnected(device: BluetoothDevice) {}

    /**
     * 绑定时触发。
     */
    fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo? = null) {}

    /**
     * 解绑时触发。
     */
    fun onIdentityDelete(status: Boolean) {}

    /**
     * 连接状态变化时触发。
     */
    fun onSessionStateChange(status: Boolean) {}

    /**
     * 设备回复某些指令时触发。
     */
    fun onCommandReply(bleKey: BleKey, bleKeyFlag: BleKeyFlag, status: Boolean) {}

    /**
     * 设备进入OTA时触发。
     */
    fun onOTA(status: Boolean) {}

    /**
     * MTK设备返回固件信息，该信息需要通过[BleConnector.SERVICE_MTK]和[BleConnector.CH_MTK_OTA_META]来读取，
     * 设备返回该信息后会通过[BleCache.putMtkOtaMeta]保存该信息，然后通过[BleCache.getMtkOtaMeta]可以获取该信息。
     * mid=xx;mod=xx;oem=xx;pf=xx;p_id=xx;p_sec=xx;ver=xx;d_ty=xx;
     */
    fun onReadMtkOtaMeta() {}

    fun onXModem(status: Byte) {}

    /**
     * 设备返回电量时触发。
     */
    fun onReadPower(power: Int) {}

    /**
     * 设备返回固件版本时触发。
     */
    fun onReadFirmwareVersion(version: String) {}

    /**
     * 设备返回mac地址时触发。
     */
    fun onReadBleAddress(address: String) {}

    /**
     * 设备返回久坐设置时触发。
     */
    fun onReadSedentariness(sedentarinessSettings: BleSedentarinessSettings) {}

    /**
     * 设备返回勿扰设置时触发。
     */
    fun onReadNoDisturb(noDisturbSettings: BleNoDisturbSettings) {}

    /**
     * 设备端修改勿扰设置时触发。
     */
    fun onNoDisturbUpdate(noDisturbSettings: BleNoDisturbSettings) {}

    /**
     * 设备返回闹钟列表时触发。
     */
    fun onReadAlarm(alarms: List<BleAlarm>) {}

    /**
     * 设备端修改闹钟时触发。
     */
    fun onAlarmUpdate(alarm: BleAlarm) {}

    /**
     * 设备端删除闹钟时触发。
     */
    fun onAlarmDelete(id: Int) {}

    /**
     * 设备端创建闹钟时触发。
     */
    fun onAlarmAdd(alarm: BleAlarm) {}

    /**
     * 设备返回Coaching id时触发。
     */
    fun onReadCoachingIds(bleCoachingIds: BleCoachingIds) {}

    /**
     * 当设备发起找手机触发。
     */
    fun onFindPhone(start: Boolean) {}

    /**
     * 设备返回UI包版本时触发，[BleDeviceInfo.PLATFORM_REALTEK]专属。
     */
    fun onReadUiPackVersion(version: String) {}

    /**
     * 设备返回语言包信息时触发，[BleDeviceInfo.PLATFORM_REALTEK]专属。
     */
    fun onReadLanguagePackVersion(version: BleLanguagePackVersion) {}

    /**
     * 设备点击音乐相关按键时触发。
     */
    fun onReceiveMusicCommand(musicCommand: MusicCommand) {}

    /**
     * 同步数据时触发。
     * @param syncState [SyncState]
     * @param bleKey [BleKey]
     */
    fun onSyncData(syncState: Int, bleKey: BleKey) {}

    /**
     * 当设备返回[BleActivity]时触发。
     */
    fun onReadActivity(activities: List<BleActivity>) {}

    /**
     * 当设备返回[BleHeartRate]时触发。
     */
    fun onReadHeartRate(heartRates: List<BleHeartRate>) {}

    /**
     * 当设备返回[BleBloodPressure]时触发。
     */
    fun onReadBloodPressure(bloodPressures: List<BleBloodPressure>) {}

    /**
     * 当设备返回[BleSleep]时触发。
     */
    fun onReadSleep(sleeps: List<BleSleep>) {}

    /**
     * 当设备返回[BleWorkout]时触发。
     */
    fun onReadWorkout(workouts: List<BleWorkout>) {}

    /**
     * 当设备返回[BleLocation]时触发。
     */
    fun onReadLocation(locations: List<BleLocation>) {}

    /**
     * 当设备返回[BleTemperature]时触发。
     */
    fun onReadTemperature(temperatures: List<BleTemperature>) {}

    /**
     * 当设备返回[BleBloodOxygen]时触发。
     */
    fun onReadBloodOxygen(bloodOxygen: List<BleBloodOxygen>) {}

    /**
     * 当设备返回[BleHrv]时触发。
     */
    fun onReadBleHrv(hrv: List<BleHrv>) {}

    /**
     * 设备主动执行拍照相关操作时触发。
     * @param cameraState [CameraState]
     */
    fun onCameraStateChange(cameraState: Int) {}

    /**
     * 手机执行拍照相关操作，设备回复时触发。
     * 手机发起后设备响应。用于确认设备是否能立即响应手机发起的操作，比如设备在某些特定界面是不能进入相机的，
     * 如果手机发起进入相机指令，设备会回复失败
     * @param cameraState [CameraState]
     */
    fun onCameraResponse(status: Boolean, cameraState: Int) {}

    /**
     * 调用[BleConnector.sendStream]后触发，用于回传发送进度。
     */
    fun onStreamProgress(status: Boolean, errorCode: Int, total: Int, completed: Int) {}

    /**
     * 设备请求定位时触发，一些无Gps设备在锻炼时会请求手机定位。
     * @param workoutState [WorkoutState]
     */
    fun onRequestLocation(workoutState: Int) {}

    /**
     * 设备开启Gps时，如果检测到没有aGps文件，或aGps文件已过期，设备发起请求aGps文件
     * @param url aGps文件的下载链接
     */
    fun onDeviceRequestAGpsFile(url: String) {}

    /**
     * 当设备控制来电时触发。  0 -接听 ； 1-拒接
     * @param url aGps文件的下载链接
     */
    fun onIncomingCallStatus(status: Int) {}
}
