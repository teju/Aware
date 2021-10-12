package com.watch.aware.app.fragments.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.*

import com.watch.aware.app.MainActivity
import com.watch.aware.app.R
import com.watch.aware.app.callback.EditSlotsListener
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.callback.PermissionListener
import com.watch.aware.app.fragments.dialog.EditSleepDialogFragment
import com.watch.aware.app.fragments.dialog.NotifyDialogFragment
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper.isEmpty
import com.watch.aware.app.models.BaseParams
import com.watch.aware.app.models.Steps
import com.watch.aware.app.webservices.PostSaveDeviceDataViewModel
import com.yc.pedometer.info.Rate24HourDayInfo
import com.yc.pedometer.info.StepOneHourInfo
import com.yc.pedometer.info.TemperatureInfo
import com.yc.pedometer.sdk.*
import com.yc.pedometer.update.Updates
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


open class BaseFragment : GenericFragment() {

     var mDataProcessing: DataProcessing? = null
     var mySQLOperate: UTESQLOperate? = null
     var mWriteCommand: WriteCommandToBLE? = null
     var mContext: Context? = null

     val UPDATE_STEP_UI_MSG = 0
     val UPDATE_SLEEP_UI_MSG = 1
     val DISCONNECT_MSG = 18
     val CONNECTED_MSG = 19
     val UPDATA_REAL_RATE_MSG = 20
     val RATE_SYNC_FINISH_MSG = 21
     val OPEN_CHANNEL_OK_MSG = 22
     val CLOSE_CHANNEL_OK_MSG = 23
     val TEST_CHANNEL_OK_MSG = 24
     val OFFLINE_SWIM_SYNC_OK_MSG = 25
     val OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG = 30
     val SERVER_CALL_BACK_OK_MSG = 31
     val OFFLINE_SKIP_SYNC_OK_MSG = 32
     val test_mag1 = 35
     val test_mag2 = 36
     val OFFLINE_STEP_SYNC_OK_MSG = 37
     val UPDATE_SPORTS_TIME_DETAILS_MSG = 38

     val UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG = 39 //sdk发送数据到ble完成，并且校验成功，返回状态

     val UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG = 40 //sdk发送数据到ble完成，但是校验失败，返回状态

     val UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG = 41 //ble发送数据到sdk完成，并且校验成功，返回数据

     val UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL_MSG = 42 //ble发送数据到sdk完成，但是校验失败，返回状态


     val RATE_OF_24_HOUR_SYNC_FINISH_MSG = 43
     val BIND_CONNECT_SEND_ACCOUNT_ID_MSG = 44


     val TIME_OUT_SERVER: Long = 10000
     val TIME_OUT: Long = 120000
     var isUpdateSuccess = false

     var mUpdates: Updates? = null
     var mBLEServiceOperate: BLEServiceOperate? = null
     var mBluetoothLeService: BluetoothLeService? = null

    // caicai add for sdk
    val EXTRAS_DEVICE_NAME = "device_name"
    val EXTRAS_DEVICE_ADDRESS = "device_address"
     val CONNECTED = 1
     val CONNECTING = 2
     val DISCONNECTED = 3
     var CURRENT_STATUS = DISCONNECTED

     var mDeviceName: String? = null
     var mDeviceAddress: String? = null

     var tempRate = 70
     var tempStatus = 0

     val resultBuilder = StringBuilder()
    val testKey1 =
        "00a4040008A000000333010101000003330101010000333010101000033301010100003330101010000033301010100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100a4040008A0000003330101010000033301010100003330101010000333010101000033301010100000333010101003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101"
    val universalKey =
        "040008A0000040008A00000033301010100000333010101000033301010100003330101010000333010101000003330100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010040008A000000333010101000003330101010000333010101000033301010100003330101010000033301003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330100033301010100000333010101000033301010100003330101010000333010101000003330100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010"


    var permissionsThatNeedTobeCheck: List<String> =
        ArrayList()
    var permissionListener: PermissionListener? = null
    var v: View? = null
    var baseParams = BaseParams()
    open fun onBackTriggered() {
        home()!!.proceedDoOnBackPressed()
    }

    fun home(): MainActivity? {
        return activity as MainActivity?
    }

    var obsNoInternet =
        object : Observer<Boolean> {
            // $FF: synthetic method
            // $FF: bridge method


            override fun onChanged(isHaveInternet: Boolean?) {
                try {
                    if (!isHaveInternet!!) {
                        if (this@BaseFragment.activity == null) {
                            return
                        }
                        showNotifyDialog(
                            activity!!.getString(R.string.no_internet),
                            getString(R.string.no_connection), "OK",
                            "", NotifyListener { }
                        )
                    }
                } catch (var3: Exception) {
                }
            }
        } as Observer<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions: MutableList<String> =
            ArrayList()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        permissions.add(Manifest.permission.BLUETOOTH)
        checkPermissions(permissions, permissionListener)


        checkBluetoothGps()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSaveDeviceDataAPIObserver()
    }

    fun checkBluetoothGps() {
        val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val locationManager: LocationManager? = activity?.getSystemService(LOCATION_SERVICE) as LocationManager?

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            showNotifyDialog("","Please Enable your bluetooth",
                "OK","Cancel",object :NotifyListener {
                    override fun onButtonClicked(which: Int) {
                        if(which == NotifyDialogFragment.BUTTON_POSITIVE) {
                            val intentOpenBluetoothSettings = Intent()
                            intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS)
                            startActivity(intentOpenBluetoothSettings)
                        }
                    }
                })

        } else  if (!locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!){
            showNotifyDialog("","Please Enable your Gps",
                "OK","Cancel",object :NotifyListener {
                    override fun onButtonClicked(which: Int) {
                        if(which == NotifyDialogFragment.BUTTON_POSITIVE) {
                            val intentOpenBluetoothSettings = Intent()
                            intentOpenBluetoothSettings.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(intentOpenBluetoothSettings)
                        }
                    }
                })

        }
    }
    fun checkPermissions(
        permissionsThatNeedTobeCheck: MutableList<String>,
        permissionListener: PermissionListener?
    ) {
        this.permissionsThatNeedTobeCheck = permissionsThatNeedTobeCheck
        this.permissionListener = permissionListener
        val permissionsNeeded =
            ArrayList<String>()
        val permissionsList =
            permissionsThatNeedTobeCheck
        try {
            for (s in permissionsThatNeedTobeCheck) {
                if (s == Manifest.permission.CAMERA) {
                    if (!addPermission(
                            permissionsList,
                            Manifest.permission.CAMERA
                        )
                    ) permissionsNeeded.add("Camera") else if (s.equals(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            ignoreCase = true
                        )
                    ) {
                        if (!addPermission(
                                permissionsList,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) permissionsNeeded.add("ACCESS COARSE LOCATION")
                    } else if (s.equals(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            ignoreCase = true
                        )
                    ) {
                        if (!addPermission(
                                permissionsList,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        ) permissionsNeeded.add("ACCESS FINE LOCATION")
                    }
                }
            }
        } catch (e: Exception) {
        }
        if (permissionsList.size > 0) {
            if (permissionsNeeded.size > 0) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    permissionsList.toTypedArray(),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
                )
                return
            }
            ActivityCompat.requestPermissions(
                activity!!, permissionsList.toTypedArray(),
                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
            )
            return
        } else {
            permissionListener!!.onPermissionAlreadyGranted()
        }
    }

    fun addPermission(
        permissionsList: MutableList<String>,
        permission: String
    ): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity!!,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsList.add(permission)
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    permission
                )
            ) return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
                var isAllGranted = false
                val index = 0
                for (permission in permissionsThatNeedTobeCheck) {
                    if (permission.equals(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            ignoreCase = true
                        )
                    ) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            ignoreCase = true
                        )
                    ) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    }
                }
                if (isAllGranted) {
                    permissionListener!!.onCheckPermission(permissions[index], true)
                } else {
                    permissionListener!!.onCheckPermission(permissions[index], false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* public void setBackButtonToolbarStyleOne(View v) {
        try {
            RelativeLayout llBack = v.findViewById(R.id.llBack);

                    llBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            home()?.onBackPressed();
                        }
                    });
        } catch (Exception e) {
        }
    }*/

    fun showNotifyDialog(
        tittle: String?,
        messsage: String?,
        button_positive: String?,
        button_negative: String?,
        n: NotifyListener?
    ) {
        val f = NotifyDialogFragment()
        f.listener = n
        f.notify_tittle = tittle!!
        f.notify_messsage = messsage!!
        f.button_positive = button_positive!!
        f.button_negative = button_negative!!
        f.isCancelable = false
        if (!isEmpty(tittle) || !isEmpty(
                messsage
            )
        ) {
            f.show(activity!!.supportFragmentManager, NotifyDialogFragment.TAG)
        }
    }
    open fun showEDitSlotsDialog(n: EditSlotsListener?) {
        val f = EditSleepDialogFragment()
        f.listener = n
        f.setCancelable(true)
        f.show(activity!!.supportFragmentManager, EditSleepDialogFragment.TAG)
    }

    fun heartRateInsert(heartRates:List<Rate24HourDayInfo>) {
        val dataBaseHelper = DataBaseHelper(activity)
        for(heart in heartRates) {
            val startDate = BaseHelper.parseDate(heart.calendar, WATCHDate)
            var mtime = (heart.time.toDouble() / 60.0).toString()
            mtime = String.format("%.2f", mtime.toFloat())
            val startTime = BaseHelper.parseDate(mtime, TIME_JSON_HM)
            dataBaseHelper.heartInsert(dataBaseHelper,heart.rate,
                BaseHelper.parseDate(startDate, Constants.DATE_JSON),
                mtime)
        }
    }
    /*fun SpoRateInsert(bloodOxygen: List<BleBloodOxygen>) {
        val dataBaseHelper = DataBaseHelper(activity)
        for(bloodOxyge in bloodOxygen) {
            dataBaseHelper.SPoInsert(dataBaseHelper,bloodOxyge.mValue,BaseHelper.parseDate(Date(),
                Constants.DATE_JSON),epcoToTime(bloodOxyge.mTime))
        }
    }*/

    fun TempInsert(temp: TemperatureInfo) {
        val dataBaseHelper = DataBaseHelper(activity)
        val mtime = (temp.secondTime.toDouble() / 60.0).toString()
        val startTime = BaseHelper.parseDate(mtime, TIME_JSON_HM)
        val startDate = BaseHelper.parseDate(temp.startDate, WATCHTIME)
        dataBaseHelper.TempInsert(
            dataBaseHelper, temp.bodyTemperature.toDouble(),
            BaseHelper.parseDate(startDate, Constants.DATE_JSON),
            BaseHelper.parseDate(startTime, TIME_JSON_HM)
        )


    }

    fun insertStepData(activities: java.util.ArrayList<StepOneHourInfo>,
                       calendar: String,total_steps:String,total_cal:String,total_dist : String) {
        try {
            val dataBaseHelper = DataBaseHelper(activity)
            for (activity in activities) {
                val startDate = BaseHelper.parseDate(calendar, WATCHDate)
                val dist = (activity.step.toDouble() / 1320.0).toFloat()
                val cal =(activity.step.toDouble() / 40.0).toFloat()
                var mtime = (activity.time.toDouble() / 60.0).toString()
                if(!isTimeInserted(mtime)) {
                    dataBaseHelper.stepsInsert(
                        dataBaseHelper,
                        activity.step.toString(),
                        BaseHelper.parseDate(startDate, Constants.DATE_JSON),
                        String.format("%.3f", dist),
                        String.format("%.2f", cal).toString(),
                        mtime,total_steps,total_cal,total_dist
                    )
                } else {
                    dataBaseHelper.deleteSteps(mtime,BaseHelper.parseDate(startDate, Constants.DATE_JSON))
                    dataBaseHelper.stepsInsert(
                        dataBaseHelper,
                        activity.step.toString(),
                        BaseHelper.parseDate(startDate, Constants.DATE_JSON),
                        String.format("%.3f", dist),
                        String.format("%.2f", cal).toString(),
                        mtime,total_steps,total_cal,total_dist)
                }
            }
        } catch (e:Exception){
            e.toString()
        }
    }

    fun lastestHRSteps(): List<Steps>? {
        val dataBaseHelper = DataBaseHelper(activity)
        val dteps = dataBaseHelper.getAllSteps(" ORDER BY date DESC,time DESC")
        return dteps
    }
    fun isTimeInserted(time : String): Boolean {
        val dataBaseHelper = DataBaseHelper(activity)
        val dteps = dataBaseHelper.getAllSteps(
            "WHERE date is  ('" + BaseHelper.parseDate(Date(), Constants.DATE_JSON) + "') " +
                " AND time is '"+time+"'")
        if(dteps.size > 0) {
            return true
        } else {
            return false
        }
    }

    fun setSaveDeviceDataAPIObserver() {
        try {
            postSaveDeviceDataViewModel =
                ViewModelProviders.of(this).get(PostSaveDeviceDataViewModel::class.java!!)
        } catch (e :Exception){

        }
    }


    fun epcoToTime(date : Int) :String {
        val date = Date()
        val format: DateFormat = SimpleDateFormat(TIME_JSON_HM)
        var formatted: String = format.format(date)
        if(formatted.contains(":")) {
            formatted = formatted.replace(":",".")
        }
        return formatted
    }

    companion object {
        var REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 911
        var postSaveDeviceDataViewModel: PostSaveDeviceDataViewModel? = null
    }
}