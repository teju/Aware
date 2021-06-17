package com.watch.aware.app.helper

import com.iapps.libs.helpers.BaseKeys

class APIs : BaseKeys() {
    companion object {
        val BASE_URL = "https://asia-east2-cband2.cloudfunctions.net/"

        val postRegister : String
            get() = BASE_URL!!  + "Insert_User"

        val postSaveDeviceData : String
            get() = BASE_URL!!  + "Save_DeviceData"

        val postGetCovidStatus : String
            get() = BASE_URL!!  + "Get_CovidStatus"


    }
}