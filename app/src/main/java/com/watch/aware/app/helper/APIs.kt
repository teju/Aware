package com.watch.aware.app.helper

import com.iapps.libs.helpers.BaseKeys

class APIs : BaseKeys() {
    companion object {
        val BASE_URL = "https://asia-east2-cband2.cloudfunctions.net/"

        val postRegister : String
            get() = BASE_URL!!  + "Register_Profile"

        val postLogin : String
            get() = BASE_URL!!  + "Login"

        val postProfileUpdate : String
            get() = BASE_URL!!  + "Update_Profile"

        val postSaveDeviceData : String
            get() = BASE_URL!!  + "Save_DeviceData"

        val postGetCovidStatus : String
            get() = BASE_URL!!  + "Get_CovidStatus"


    }
}