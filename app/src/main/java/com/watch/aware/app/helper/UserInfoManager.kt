package com.watch.aware.app.helper

import android.content.Context
import android.content.SharedPreferences

class UserInfoManager private constructor() {

    private val KEY_ACCESS_TOKEN = "F3ZT7"
    private val KEY_ACCOUNT_ID = "V8D85H"
    private val KEY_ACCOUNT_NAME = "key_account_name"
    private val KEY_EMAIL = "key_email"
    private val KEY_CONTACT_NUMBER = "key_contact_number"
    private val KEY_AGE = "key_age"
    private val KEY_GENDER = "key_gender"
    private val KEY_LOGGED_IN = "key_logged_in"
    private var accessToken: String? = null
    private var accountName: String? = ""
    private var email: String? = ""
    private var contact_number: String? = ""
    private var age: String? = ""
    private var gender: String? = ""
    private var isLoggedIn: Boolean = false


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

    fun saveIsLoggedIn(isLoggedIn: Boolean) {
        this.isLoggedIn = isLoggedIn
        val editor = this.prefs!!.edit()
        editor.putBoolean(KEY_LOGGED_IN, isLoggedIn)
        editor.commit()
    }

    fun getISLoggedIn(): Boolean {
        this.isLoggedIn = this.prefs!!.getBoolean(KEY_LOGGED_IN, false)
        return isLoggedIn!!
    }
    fun getEmail(): String {
        this.email = this.prefs!!.getString(KEY_EMAIL, "")
        return email!!
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

    fun logout() {
        saveAuthToken(null)
        prefs!!.edit().clear().commit()
        _userInfo = null
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