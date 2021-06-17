package com.watch.aware.app.helper

import com.iapps.libs.helpers.BaseKeys

class Keys : BaseKeys() {
    companion object {
        @kotlin.jvm.JvmField
        var GCM_MESSAGE: String = "message"
        val MESSAGE = "message"
        val STATUS_CODE = "success"
        val FAILED = "failed"
        @kotlin.jvm.JvmField
        val TAG = "tag"
        @kotlin.jvm.JvmField
        val SLIDE_TELLER_NEW_REQUEST_MESSAGE = "SLIDE_TELLER_NEW_REQUEST_MESSAGE"
        @kotlin.jvm.JvmField
        val MESSAGE_TYPE = "message_type"
        @kotlin.jvm.JvmField
        val SLIDE_TELLER_NEW_REQUEST_TAG = "SLIDE_TELLER_NEW_REQUEST_TAG"
        @kotlin.jvm.JvmField
        val NEARBYTELLERSERVICERECOMMEND = "NearbyTellerServiceRecommend"
        @kotlin.jvm.JvmField
        val UPDATE_SUPER_MAIN_ACTIVITY = "UPDATE_SUPER_MAIN_ACTIVITY"
        @kotlin.jvm.JvmField
        val NEARBYTELLERSERVICECOMPELETE = "NearbyTellerServiceCompleted"

        val username = "username"
        val age = "age"
        val contactNumber = "contactNumber"
        val email = "email"
        val deviceId = "deviceId"
        val SPO2 = "SPO2"
        val HR = "HR"
        val Temp = "Temp"
        val cough = "cough"
        val activity = "activity"
        val timestamp = "timestamp"

        @kotlin.jvm.JvmField
        var GCM_DT : String = "dt"
        @kotlin.jvm.JvmField
        var GCM_COLLAPSE_KEY : String = "collapse_key"
        @kotlin.jvm.JvmField
        var GCM_T : String = "t"
        @kotlin.jvm.JvmField
        var GCM_ALERT : String = "alert"
        @kotlin.jvm.JvmField
        var GCM_SOUND : String = "sound"
        @kotlin.jvm.JvmField
        var GCM_PUSHID = "pushid"
        @kotlin.jvm.JvmField
        var GCM_TYPE : String = "type"
    }
}