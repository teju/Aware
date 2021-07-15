package com.watch.aware.app.helper

import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_hM

class Constants  {
    companion object {

        // If true all the data load will ignore the location
        // As the emulator doesnt provide location based functions
        val IS_DEBUGGING = false

        var TYPE_STUDENT = "student"
        var TYPE_STAFF = "staff"
        val REQUEST_CODE_QR_CODE = 1231
        var COUGH = 0
        var SPO2 = 0
        var HR = 0
        var Temp = 0.0
        var _activity = 68
        var TWENTY_HOUR_FORMAT  = 1
        var TWELVE_HOUR_FORMAT = 0
        var TIMEFORMAT = TIME_hM
        var GOAL_TYPE = 0
        val STEPS_GOAL = 0
        val CAL_GOAL = 1
        val DIST_GOAL = 2

    }
}