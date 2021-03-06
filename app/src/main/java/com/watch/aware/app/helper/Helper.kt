package com.watch.aware.app.helper

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import com.bestmafen.baseble.data.BleReadable
import com.bestmafen.baseble.util.BleLog
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.szabh.androidblesdk3.firmware.FirmwareHelper
import com.szabh.androidblesdk3.tools.doBle
import com.szabh.androidblesdk3.tools.require
import com.szabh.androidblesdk3.tools.toast
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.*
import com.szabh.smable3.entity.*
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.concurrent.thread
import kotlin.random.Random

object Helper {
    fun hideKeyboard(activity: Activity) {
        val imm =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun isEmpty(string: String?): Boolean {
        return if (string == null || string.trim { it <= ' ' }.length == 0) {
            true
        } else false
    }

    fun dateFormat(format: String, date: Date?): String {
        val simpleDateFormat = SimpleDateFormat(format)
        return simpleDateFormat.format(date)
    }

    fun convertStringToDate(format: String?, date: String?): Date? {
        val dateFormat: DateFormat =
            SimpleDateFormat(format, Locale.ENGLISH)
        val parse: Date
        parse = try {
            dateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }
        return parse
    }

    fun findDifferenceBetweenDates(
        startDate: Date,
        endDate: Date
    ): Long {
        val duration = endDate.time - startDate.time
        return TimeUnit.MILLISECONDS.toDays(duration)
    }

    fun isNetworkAvailable(ctx: Context): Boolean {
        return try {
            val manager =
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo
            var isAvailable = false
            if (networkInfo != null && networkInfo.isConnected) {
                isAvailable = true
            }
            if (!isAvailable) {
            }
            isAvailable
        } catch (e: Exception) {
            true
        }
    }

    fun listFragmentsMainTab(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("FIRST_TAB")
        list.add("SECOND_TAB")
        list.add("THIRD_TAB")
        return list
    }

    fun isValidMobile(phone: String): Boolean {
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length == 10
        } else false
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(
            target
        ).matches()
    }

    fun visibleView(v: View?) {
        if (v != null) {
            v.visibility = View.VISIBLE
        }
    }

    fun goneView(v: View?) {
        if (v != null) {
            v.visibility = View.GONE
        }
    }



    private fun setCameraIntents(
        cameraIntents: MutableList<Intent>,
        output: Uri,
        context: Context
    ) {
        val captureIntent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = context.packageManager
        val listCam =
            packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val intent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(context.packageManager) != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output)
                cameraIntents.add(intent)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val isKitKat =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                try {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(
                        context,
                        contentUri,
                        null,
                        null
                    )
                } catch (e: Exception) {
                    val fileName =
                        getFilePath(context, uri)
                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory()
                            .toString() + "/Download/" + fileName
                    }
                    e.printStackTrace()
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun showImage(reg_real_Path: String?): Bitmap? {
        val imgFile = File(reg_real_Path)
        var myBitmap: Bitmap? = null
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        }
        return myBitmap
    }

    fun handleCommand(bleKey: BleKey, bleKeyFlag: BleKeyFlag,mContext:Context) {
        val db = DataBaseHelper(mContext)
        if (bleKey == BleKey.IDENTITY) {
            if (bleKeyFlag == BleKeyFlag.DELETE) { // ????????????
                if (BleConnector.isAvailable()) {
                    // ???????????????, ????????????????????????, ????????????????????????BleHandleCallback.onIdentityDelete()
                    BleConnector.sendData(bleKey, bleKeyFlag)
                } else {
                    // ???????????????, ??????????????????
                    BleConnector.unbind()

                }
                return
            }
        }

        doBle(mContext) {
            when (bleKey) {
                // BleCommand.UPDATE
                BleKey.OTA -> FirmwareHelper.gotoOta(mContext)
                BleKey.XMODEM -> BleConnector.sendData(bleKey, bleKeyFlag)

                // BleCommand.SET
                BleKey.TIME -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ????????????, ???????????????????????????????????????
                        BleConnector.sendObject(bleKey, bleKeyFlag, BleTime.local())
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                BleKey.TIME_ZONE -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ????????????
                        BleConnector.sendObject(bleKey, bleKeyFlag, BleTimeZone())
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                BleKey.POWER -> BleConnector.sendData(bleKey, bleKeyFlag) // ????????????
                BleKey.FIRMWARE_VERSION -> BleConnector.sendData(bleKey, bleKeyFlag) // ??????????????????
                BleKey.BLE_ADDRESS -> BleConnector.sendData(bleKey, bleKeyFlag) // ??????BLE????????????
                BleKey.USER_PROFILE -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ??????????????????
                        val bleUserProfile = BleUserProfile(
                            mUnit = BleUserProfile.METRIC,
                            mGender = BleUserProfile.FEMALE,
                            mAge = 20,
                            mHeight = 170f,
                            mWeight = 60f)
                        BleConnector.sendObject(bleKey, bleKeyFlag, bleUserProfile)
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                BleKey.STEP_GOAL -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ??????????????????
                        BleConnector.sendInt32(bleKey, bleKeyFlag, 0x1234)
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                BleKey.BACK_LIGHT -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ??????????????????
                        BleConnector.sendInt8(bleKey, bleKeyFlag, 6) // 0 is off, or 5 ~ 20
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                BleKey.SEDENTARINESS -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ????????????
                        val bleSedentariness = BleSedentarinessSettings(
                            mEnabled = 1,
                            // Monday ~ Saturday
                            mRepeat = BleRepeat.MONDAY or BleRepeat.TUESDAY or BleRepeat.WEDNESDAY or BleRepeat.THURSDAY or BleRepeat.FRIDAY or BleRepeat.SATURDAY,
                            mStartHour = 1,
                            mEndHour = 22,
                            mInterval = 60
                        )
                        BleConnector.sendObject(bleKey, bleKeyFlag, bleSedentariness)
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                BleKey.NO_DISTURB_RANGE -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ????????????
                        val noDisturb = BleNoDisturbSettings().apply {
                            mBleTimeRange1 = BleTimeRange(1, 2, 0, 18, 0)
                        }
                        BleConnector.sendObject(bleKey, bleKeyFlag, noDisturb)
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
//                BleKey.NO_DISTURB_GLOBAL -> BleConnector.sendBoolean(bleKey, bleKeyFlag, true) // on
                BleKey.VIBRATION -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ??????????????????
                        BleConnector.sendInt8(bleKey, bleKeyFlag, 3) // 0~10, 0 is off
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                BleKey.GESTURE_WAKE -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ???????????????
                        BleConnector.sendObject(bleKey, bleKeyFlag,
                            BleGestureWake(BleTimeRange(1, 8, 0, 22, 0)))
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
//                BleKey.HR_ASSIST_SLEEP -> BleConnector.sendBoolean(bleKey, bleKeyFlag, true) // on
                // ???????????????
                BleKey.HOUR_SYSTEM ->
                    // ??????, 0: 24-hourly; 1: 12-hourly
                    BleConnector.sendInt8(bleKey, bleKeyFlag, BleCache.getInt(bleKey, 0) xor 1)
                // ??????????????????
                BleKey.LANGUAGE -> BleConnector.sendInt8(bleKey, bleKeyFlag, Languages.languageToCode())
                BleKey.ALARM -> {
                    // ????????????1??????????????????
                    if (bleKeyFlag == BleKeyFlag.CREATE) {
                        val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 1) }
                        BleConnector.sendObject(bleKey, bleKeyFlag,
                            BleAlarm(
                                mEnabled = 1,
                                mRepeat = BleRepeat.EVERYDAY,
                                mYear = calendar.get(Calendar.YEAR),
                                mMonth = calendar.get(Calendar.MONTH) + 1,
                                mDay = calendar.get(Calendar.DAY_OF_MONTH),
                                mHour = calendar.get(Calendar.HOUR_OF_DAY),
                                mMinute = calendar.get(Calendar.MINUTE),
                                mTag = "tag"))
                    } else if (bleKeyFlag == BleKeyFlag.DELETE) {
                        // ????????????????????????????????????????????????
                        val alarms = BleCache.getList(BleKey.ALARM, BleAlarm::class.java)
                        if (alarms.isNotEmpty()) {
                            BleConnector.sendInt8(bleKey, bleKeyFlag, alarms[0].mId)
                        }
                    } else if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ?????????????????????????????????????????????????????????????????????
                        val alarms = BleCache.getList(BleKey.ALARM, BleAlarm::class.java)
                        if (alarms.isNotEmpty()) {
                            alarms[0].let { alarm ->
                                alarm.mEnabled = if (alarm.mEnabled == 0) 1 else 0
                                BleConnector.sendObject(bleKey, bleKeyFlag, alarm)
                            }
                        }
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        // ??????????????????????????????
                        BleConnector.sendInt8(bleKey, bleKeyFlag, ID_ALL)
                    } else if (bleKeyFlag == BleKeyFlag.RESET) {
                        // ????????????????????????
                        val calendar = Calendar.getInstance()
                        BleConnector.sendList(bleKey, bleKeyFlag, List(8) {
                            BleAlarm(
                                mEnabled = it.rem(2),
                                mRepeat = it,
                                mYear = calendar.get(Calendar.YEAR),
                                mMonth = calendar.get(Calendar.MONTH) + 1,
                                mDay = calendar.get(Calendar.DAY_OF_MONTH),
                                mHour = calendar.get(Calendar.HOUR_OF_DAY),
                                mMinute = it,
                                mTag = "$it")
                        })
                    }
                }
                BleKey.COACHING -> {
                    if (bleKeyFlag == BleKeyFlag.CREATE) {
                        BleConnector.sendObject(bleKey, bleKeyFlag, BleCoaching(
                            "My title", // title
                            "My description", // description
                            3, // repeat
                            listOf(BleCoachingSegment(
                                CompletionCondition.DURATION.condition, // completion condition
                                "My name", // name
                                0, // activity
                                Stage.WARM_UP.stage, // stage
                                10, // completion value
                                0 // hr zone
                            ))
                        ))
                    } else if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ??????????????????Coaching????????????????????????Coaching?????????
                        val coachings = BleCache.getList(BleKey.COACHING, BleCoaching::class.java)
                        if (coachings.isNotEmpty()) { // update the first coaching
                            coachings[0].let { coaching ->
                                coaching.mTitle += "nice"
                                BleConnector.sendObject(bleKey, bleKeyFlag, coaching)
                            }
                        }
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        // ????????????Coaching
                        BleConnector.sendInt8(bleKey, bleKeyFlag, ID_ALL)
                    }
                }
                // ??????????????????????????????
                BleKey.NOTIFICATION_REMINDER ->
                    BleConnector.sendBoolean(bleKey, bleKeyFlag, !BleCache.getBoolean(bleKey, false)) // ??????
                // ??????????????????
                BleKey.ANTI_LOST ->
                    BleConnector.sendBoolean(bleKey, bleKeyFlag, !BleCache.getBoolean(bleKey, false)) // ??????
                // ????????????????????????
                BleKey.HR_MONITORING -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        val hrMonitoring = BleHrMonitoringSettings(
                            mBleTimeRange = BleTimeRange(1, 8, 0, 22, 0),
                            mInterval = 60 // an hour
                        )
                        BleConnector.sendObject(bleKey, bleKeyFlag, hrMonitoring)
                    } else if (bleKeyFlag == BleKeyFlag.READ) {
                        BleConnector.sendData(bleKey, bleKeyFlag)
                    }
                }
                // ??????UI?????????
                BleKey.UI_PACK_VERSION -> BleConnector.sendData(bleKey, bleKeyFlag)
                // ?????????????????????
                BleKey.LANGUAGE_PACK_VERSION -> BleConnector.sendData(bleKey, bleKeyFlag)
                // ??????????????????
                BleKey.SLEEP_QUALITY -> BleConnector.sendObject(bleKey, bleKeyFlag,
                    BleSleepQuality(mLight = 202, mDeep = 201, mTotal = 481))
                // ????????????????????????
                BleKey.GIRL_CARE -> BleConnector.sendObject(bleKey, bleKeyFlag,
                    BleGirlCareSettings(
                        mEnabled = 1,
                        mReminderHour = 9,
                        mReminderMinute = 0,
                        mMenstruationReminderAdvance = 2,
                        mOvulationReminderAdvance = 2,
                        mLatestYear = 2020,
                        mLatestMonth = 1,
                        mLatestDay = 1,
                        mMenstruationDuration = 7,
                        mMenstruationPeriod = 30
                    ))
                // ??????????????????
                BleKey.TEMPERATURE_DETECTING -> BleConnector.sendObject(bleKey, bleKeyFlag,
                    BleTemperatureDetecting(
                        mBleTimeRange = BleTimeRange(1, 8, 0, 22, 0),
                        mInterval = 60 // an hour
                    ))

                //BleCommand.CONNECT
                BleKey.IDENTITY ->
                    if (bleKeyFlag == BleKeyFlag.CREATE) {
                        // ????????????, ????????????????????????, ????????????????????????????????????
                        BleConnector.sendInt32(bleKey, bleKeyFlag, Random.nextInt())
                    }

                // BleCommand.PUSH
                BleKey.SCHEDULE -> {
                    if (bleKeyFlag == BleKeyFlag.CREATE) {
                        // ????????????1??????????????????
                        val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 1) }
                        BleConnector.sendObject(bleKey, bleKeyFlag,
                            BleSchedule(
                                mYear = calendar.get(Calendar.YEAR),
                                mMonth = calendar.get(Calendar.MONTH) + 1,
                                mDay = calendar.get(Calendar.DAY_OF_MONTH),
                                mHour = calendar.get(Calendar.HOUR_OF_DAY),
                                mMinute = calendar.get(Calendar.MINUTE),
                                mAdvance = 0,
                                mTitle = "Title8",
                                mContent = "Content9"
                            ))
                    } else if (bleKeyFlag == BleKeyFlag.DELETE) {
                        // ????????????????????????????????????????????????
                        val schedules = BleCache.getList(BleKey.SCHEDULE, BleSchedule::class.java)
                        if (schedules.isNotEmpty()) {
                            BleConnector.sendInt8(bleKey, bleKeyFlag, schedules[0].mId)
                        }
                    } else if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        // ???????????????????????????????????????????????????????????????
                        val schedules = BleCache.getList(BleKey.SCHEDULE, BleSchedule::class.java)
                        if (schedules.isNotEmpty()) {
                            schedules[0].let { schedule ->
                                schedule.mHour = Random.nextInt(23)
                                BleConnector.sendObject(bleKey, bleKeyFlag, schedule)
                            }
                        }
                    }
                }
                // ??????????????????
                BleKey.WEATHER_REALTIME -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        BleConnector.sendObject(BleKey.WEATHER_REALTIME, bleKeyFlag, BleWeatherRealtime(
                            mTime = (Date().time / 1000L).toInt(),
                            mWeather = BleWeather(
                                mCurrentTemperature = 1,
                                mMaxTemperature = 1,
                                mMinTemperature = 1,
                                mWeatherCode = BleWeather.SUNNY,
                                mWindSpeed = 1,
                                mHumidity = 1,
                                mVisibility = 1,
                                mUltraVioletIntensity = 1,
                                mPrecipitation = 1
                            )
                        ))
                    }
                }
                // ??????????????????
                BleKey.WEATHER_FORECAST -> {
                    if (bleKeyFlag == BleKeyFlag.UPDATE) {
                        BleConnector.sendObject(BleKey.WEATHER_FORECAST, bleKeyFlag, BleWeatherForecast(
                            mTime = (Date().time / 1000L).toInt(),
                            mWeather1 = BleWeather(
                                mCurrentTemperature = 2,
                                mMaxTemperature = 2,
                                mMinTemperature = 2,
                                mWeatherCode = BleWeather.CLOUDY,
                                mWindSpeed = 2,
                                mHumidity = 2,
                                mVisibility = 2,
                                mUltraVioletIntensity = 2,
                                mPrecipitation = 2),
                            mWeather2 = BleWeather(
                                mCurrentTemperature = 3,
                                mMaxTemperature = 3,
                                mMinTemperature = 3,
                                mWeatherCode = BleWeather.OVERCAST,
                                mWindSpeed = 3,
                                mHumidity = 3,
                                mVisibility = 3,
                                mUltraVioletIntensity = 3,
                                mPrecipitation = 3),
                            mWeather3 = BleWeather(
                                mCurrentTemperature = 4,
                                mMaxTemperature = 4,
                                mMinTemperature = 4,
                                mWeatherCode = BleWeather.RAINY,
                                mWindSpeed = 4,
                                mHumidity = 4,
                                mVisibility = 4,
                                mUltraVioletIntensity = 4,
                                mPrecipitation = 4)
                        ))
                    }
                }

                // BleCommand.DATA
                BleKey.DATA_ALL, BleKey.ACTIVITY, BleKey.HEART_RATE, BleKey.BLOOD_PRESSURE, BleKey.SLEEP,
                BleKey.WORKOUT, BleKey.LOCATION, BleKey.TEMPERATURE ->
                    // ????????????
                    BleConnector.sendData(bleKey, bleKeyFlag)

                // BleCommand.CONTROL

                // BleCommand.IO

                else -> {
                    toast(mContext, "$bleKey")
                }
            }
        }
    }
    fun getActivities() {


    }
}