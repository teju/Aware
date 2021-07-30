package com.watch.aware.app.helper

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService


class UserInfoManager private constructor() {

    private val KEY_ACCESS_TOKEN = "F3ZT7"
    private val KEY_ACCOUNT_ID = "V8D85H"
    private val KEY_ACCOUNT_NAME = "key_account_name"
    private val KEY_EMAIL = "key_email"
    private val KEY_CONTACT_NUMBER = "key_contact_number"
    private val KEY_AGE = "key_age"
    private val KEY_GENDER = "key_gender"
    private val KEY_HEIGHT = "key_height"
    private val KEY_WEIGHT = "key_weight"
    private val KEY_LOGGED_IN = "key_logged_in"
    private val KEY_IS_FIRST_TIME = "key_is_first_time"
    private val KEY_TIME_FORMAT = "key_time_format"
    private val KEY_SLEEP_HOURS_START = "key_sleep_hours_start"
    private val KEY_SLEEP_HOURS_END = "key_sleep_hours_end"
    private val KEY_GOAL_TYPE = "key_goal_type"
    private val KEY_GOAL_VALUE= "key_goal_value"
    private var accessToken: String? = null
    private var accountName: String? = ""
    private var email: String? = ""
    private var contact_number: String? = ""
    private var age: String? = ""
    private var gender: String? = ""
    private var height: String? = ""
    private var weight: String? = ""
    private var isLoggedIn: Boolean = false
    private var isFirstTime: Boolean = false
    private var timeFormat: Int = 0
    private var sleepHoursStart = ""
    private var sleepHourEnd  = ""
    private var goalType  = 0
    private var goalValue  = 10000


    private var prefs: SharedPreferences? = null
    private var prefsnoclear: SharedPreferences? = null

    val authToken: String?
        get() {
            if (accessToken == null) {
                this.accessToken = this.prefs!!.getString(KEY_ACCESS_TOKEN, null)
                if (this.accessToken != null)
                    if (this.accessToken!!.toLowerCase().contains("false")) {
                        this.accessToken = null
                    }
                if (accessToken == null)
                    return null
            }
            return accessToken
        }

    private fun openPrefsNoclear(c: Context) {
        this.prefsnoclear = c.getSharedPreferences(FILE_NAME_NOCLR, Context.MODE_PRIVATE)
    }

    private fun openPrefs(c: Context) {
        this.prefs = c.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuthToken(accessToken: String?) {
        this.accessToken = accessToken
        val editor = this.prefs!!.edit()
        editor.putString(KEY_ACCESS_TOKEN, this.accessToken)
        editor.commit()
    }





    fun saveAccountName(accountName: String) {
        this.accountName = accountName
        val editor = this.prefs!!.edit()
        editor.putString(KEY_ACCOUNT_NAME, accountName)
        editor.commit()
    }

    fun saveEmail(email: String) {
        this.email = email
        val editor = this.prefs!!.edit()
        editor.putString(KEY_EMAIL, email)
        editor.commit()
    }
    fun saveContactNumber(contact_number: String) {
        this.contact_number = contact_number
        val editor = this.prefs!!.edit()
        editor.putString(KEY_CONTACT_NUMBER, contact_number)
        editor.commit()
    }
    fun saveAge(age: String) {
        this.age = age
        val editor = this.prefs!!.edit()
        editor.putString(KEY_AGE, age)
        editor.commit()
    }
    fun saveGender(gender: String) {
        this.gender = gender
        val editor = this.prefs!!.edit()
        editor.putString(KEY_GENDER, gender)
        editor.commit()
    }
    fun saveSleepStartTime(sleepHoursStart: String) {
        this.sleepHoursStart = sleepHoursStart
        val editor = this.prefs!!.edit()
        editor.putString(KEY_SLEEP_HOURS_START, sleepHoursStart)
        editor.commit()
    }
    fun saveSleepEndTime(sleepHourEnd: String) {
        this.sleepHourEnd = sleepHourEnd
        val editor = this.prefs!!.edit()
        editor.putString(KEY_SLEEP_HOURS_END, sleepHourEnd)
        editor.commit()
    }

    fun saveIsLoggedIn(isLoggedIn: Boolean) {
        this.isLoggedIn = isLoggedIn
        val editor = this.prefs!!.edit()
        editor.putBoolean(KEY_LOGGED_IN, isLoggedIn)
        editor.commit()
    }
    fun saveIsFirstTime(isFirstTime: Boolean) {
        this.isFirstTime = isFirstTime
        val editor = this.prefs!!.edit()
        editor.putBoolean(KEY_IS_FIRST_TIME, isFirstTime)
        editor.commit()
    }
    fun saveTimeFormat(timeFormat: Int) {
        this.timeFormat = timeFormat
        val editor = this.prefs!!.edit()
        editor.putInt(KEY_TIME_FORMAT, timeFormat)
        editor.commit()
    }
    fun saveGoalType(goalType: Int) {
        this.goalType = goalType
        val editor = this.prefs!!.edit()
        editor.putInt(KEY_GOAL_TYPE, goalType)
        editor.commit()
    }
    fun saveGoalValue(goalValue: Int) {
        this.goalValue = goalValue
        val editor = this.prefs!!.edit()
        editor.putInt(KEY_GOAL_VALUE, goalValue)
        editor.commit()
    }
    fun saveHeightValue(height: String) {
        this.height = height
        val editor = this.prefs!!.edit()
        editor.putString(KEY_HEIGHT, height)
        editor.commit()
    }
    fun saveWeightValue(weight: String) {
        this.weight = weight
        val editor = this.prefs!!.edit()
        editor.putString(KEY_WEIGHT, weight)
        editor.commit()
    }
    fun getHeight(): String {
        this.height = this.prefs!!.getString(KEY_HEIGHT, "")
        return height!!
    }
    fun getWeight(): String {
        this.weight = this.prefs!!.getString(KEY_WEIGHT, "")
        return weight!!
    }
    fun getGoalType(): Int {
        this.goalType = this.prefs!!.getInt(KEY_GOAL_TYPE, 0)
        return goalType!!
    }
    fun getGoalValue(): Int {
        this.goalValue = this.prefs!!.getInt(KEY_GOAL_VALUE, 10000)
        return goalValue!!
    }

    fun getTimeFormat(): Int {
        this.timeFormat = this.prefs!!.getInt(KEY_TIME_FORMAT, 0)
        return timeFormat!!
    }
    fun getISLoggedIn(): Boolean {
        this.isLoggedIn = this.prefs!!.getBoolean(KEY_LOGGED_IN, false)
        return isLoggedIn!!
    }

    fun getISFirstTime(): Boolean {
        this.isFirstTime = this.prefs!!.getBoolean(KEY_IS_FIRST_TIME, false)
        return isFirstTime!!
    }

    fun getEmail(): String {
        this.email = this.prefs!!.getString(KEY_EMAIL, "")
        return email!!
    }
    fun getSleepHoursStart(): String {
        this.sleepHoursStart = this.prefs!!.getString(KEY_SLEEP_HOURS_START, "")!!
        return sleepHoursStart!!
    }
    fun getSleepHoursEnd(): String {
        this.sleepHourEnd = this.prefs!!.getString(KEY_SLEEP_HOURS_END, "")!!
        return sleepHourEnd!!
    }

    fun getAge(): String {
        this.age = this.prefs!!.getString(KEY_AGE, "")
        return age!!
    }
    fun getContactNumber(): String {
        this.contact_number = this.prefs!!.getString(KEY_CONTACT_NUMBER, "")
        return contact_number!!
    }

    fun getGEnder(): String {
        this.gender = this.prefs!!.getString(KEY_GENDER, "")
        return gender!!
    }

    fun getAccountName(): String {
        this.accountName = this.prefs!!.getString(KEY_ACCOUNT_NAME, "")
        return accountName!!
    }

    fun logout(activity : Context) {
        saveAuthToken(null)
        prefs!!.edit().clear().commit()
        clearAppData(activity)
        _userInfo = null
    }
    private fun clearAppData(activity: Context) {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                (activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager?)?.clearApplicationUserData() // note: it has a return value!
            } else {
                val packageName: String = activity.getApplicationContext().getPackageName()
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    companion object {

        private var _userInfo: UserInfoManager? = null
        private val FILE_NAME = "gon_user_sec"
        private val FILE_NAME_NOCLR = "gon_user_sec_no_clr"

        fun getInstance(c: Context): UserInfoManager {
            if (_userInfo == null) {
                _userInfo = UserInfoManager()
                _userInfo!!.openPrefs(c.applicationContext)
                _userInfo!!.openPrefsNoclear(c.applicationContext)
            }

            return _userInfo!!
        }
    }

}