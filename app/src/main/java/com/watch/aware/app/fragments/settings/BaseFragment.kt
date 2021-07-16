package com.watch.aware.app.fragments.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_JSON_HM
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.entity.BleActivity
import com.szabh.smable3.entity.BleBloodOxygen
import com.szabh.smable3.entity.BleHeartRate
import com.szabh.smable3.entity.BleTemperature
import com.watch.aware.app.MainActivity
import com.watch.aware.app.R
import com.watch.aware.app.callback.EditSlotsListener
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.callback.PermissionListener
import com.watch.aware.app.fragments.dialog.EditSleepDialogFragment
import com.watch.aware.app.fragments.dialog.NotifyDialogFragment
import com.watch.aware.app.helper.Constants.Companion.COUGH
import com.watch.aware.app.helper.Constants.Companion.HR
import com.watch.aware.app.helper.Constants.Companion.SPO2
import com.watch.aware.app.helper.Constants.Companion.Temp
import com.watch.aware.app.helper.Constants.Companion._activity
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.Helper.isEmpty
import com.watch.aware.app.models.BaseParams
import com.watch.aware.app.models.Steps
import com.watch.aware.app.webservices.PostSaveDeviceDataViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


open class BaseFragment : GenericFragment() {
    lateinit var postSaveDeviceDataViewModel: PostSaveDeviceDataViewModel

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
        permissions.add(Manifest.permission.BLUETOOTH)
        checkPermissions(permissions, permissionListener)
        if (BleCache.mDeviceInfo?.mBleName != null) {
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
        }
        checkBluetoothGps()

        setSaveDeviceDataAPIObserver()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            checkBluetoothGps()
        }
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

    fun heartRateInsert(heartRates: List<BleHeartRate>) {
        val dataBaseHelper = DataBaseHelper(activity)
        for(heartrate in heartRates) {
            dataBaseHelper.heartInsert(dataBaseHelper,heartrate.mBpm,BaseHelper.parseDate(Date(),
                Constants.DATE_JSON),epcoToDate(heartrate.mTime))
        }
        if(heartRates.get(0).mBpm != 0) {
            if (dataBaseHelper.getAllTemp(
                    "Where date is date is DATE('" + BaseHelper.parseDate(
                        Date(),
                        Constants.DATE_JSON
                    ) + "')"
                ).size == 0
            ) {
                dataBaseHelper.TempInsert(
                    dataBaseHelper, 36.1, BaseHelper.parseDate(
                        Date(),
                        Constants.DATE_JSON
                    ), epcoToDate(heartRates.get(0).mTime)
                )
            }
        }
    }
    fun SpoRateInsert(bloodOxygen: List<BleBloodOxygen>) {
        val dataBaseHelper = DataBaseHelper(activity)
        for(bloodOxyge in bloodOxygen) {
            dataBaseHelper.SPoInsert(dataBaseHelper,bloodOxyge.mValue,BaseHelper.parseDate(Date(),
                Constants.DATE_JSON),epcoToDate(bloodOxyge.mTime))
        }
    }
    fun TempInsert(temps: List<BleTemperature>) {
        val dataBaseHelper = DataBaseHelper(activity)
        for(temp in temps) {
            dataBaseHelper.TempInsert(dataBaseHelper,temp.mTemperature.toDouble(),BaseHelper.parseDate(Date(),
                Constants.DATE_JSON),epcoToDate(temp.mTime))
        }
    }

    fun insertStepData(activities: List<BleActivity>) {
        val dataBaseHelper = DataBaseHelper(activity)
            val a = activities.get(0)
        var lastHRSteps = lastHRSteps(epcoToDate(a.mTime))
        if(epcoToDate(a.mTime).toDouble() <  0.005) {
            dataBaseHelper.stepsInsert(
                dataBaseHelper,
                "0",
                BaseHelper.parseDate(Date(), Constants.DATE_JSON),
                "0",
                "0",
                epcoToDate(a.mTime), 0, 0.0,
                0, "when time is 0 min time : " + epcoToDate(a.mTime) +
                        "\ntotal_count: " + activities[0].mStep +
                        "\nlastHRSteps : " + lastHRSteps?.get(0)?.total_count?.toInt() +
                        "\nSubtract : " + ((a.mStep - lastHRSteps?.get(0)?.total_count?.toInt()!!)))

        } else if(lastHRSteps != null && lastHRSteps.size != 0) {
                val mDistance = (activities.get(0).mDistance/10000).toDouble()
                val mDist = (mDistance/1000).toDouble()
                val lasthrdist = lastHRSteps.get(0).total_dist.trim().toDouble()
                val dist : Double = mDist  - lasthrdist
                val cal : Int = (a.mCalorie / 10000).toInt()  - lastHRSteps.get(0).total_cal.toInt()
                val steps = (a.mStep - lastHRSteps.get(0).total_count.toInt())
                if(steps < 0) {
                    return
                }
                dataBaseHelper.stepsInsert(
                    dataBaseHelper,
                    steps.toString(),
                    BaseHelper.parseDate(Date(), Constants.DATE_JSON),
                    String.format("%.3f", dist),
                    (cal.toInt()).toString(),
                    epcoToDate(a.mTime), activities[0].mStep, mDist,
                    activities[0].mCalorie / 10000, " time : " + epcoToDate(a.mTime) +
                            "\ntotal_count: " + activities[0].mStep +
                            "\nlastHRSteps : " + lastHRSteps.get(0).total_count.toInt() +
                            "\nSubtract : " + ((a.mStep - lastHRSteps.get(0).total_count.toInt())))


            } else {
                if(lastHRSteps != null) {
                    val mDistance = (activities.get(0).mDistance/10000).toDouble()
                    val mDist = (mDistance/1000).toDouble()
                    dataBaseHelper.stepsInsert(
                        dataBaseHelper,
                        a.mStep.toString(),
                        BaseHelper.parseDate(Date(), Constants.DATE_JSON),
                        String.format("%.3f",mDist),
                        ((a.mCalorie / 10000)).toString(),
                        epcoToDate(a.mTime), activities[0].mStep, mDist,
                        activities[0].mCalorie/10000,
                        " time : "+epcoToDate(a.mTime)+"\ntotal_count: "+activities[0].mStep+"\nmStep : " +
                                ""+a.mStep)

                }
            }
    }

    fun lastHRSteps(mTime: String): List<Steps>? {
        val dataBaseHelper = DataBaseHelper(activity)
        val dteps = dataBaseHelper.getAllSteps("WHERE date is  ('" + BaseHelper.parseDate(Date(), Constants.DATE_JSON) + "') " +
                "AND total_count != 0  ORDER BY time DESC")
        return dteps
    }

    fun lastestHRSteps(): List<Steps>? {
        val dataBaseHelper = DataBaseHelper(activity)
        val dteps = dataBaseHelper.getAllSteps("WHERE date is  ('" + BaseHelper.parseDate(Date(), Constants.DATE_JSON) + "') " +
                " AND stepsCount != 0 ORDER BY time DESC LIMIT 1")
        return dteps
    }
    fun runTimer( ){
        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                try {
                    if(BleCache.mDeviceInfo?.mBleName != null){
                        if(SPO2 != 0 && HR != 0 && Temp != 0.0) {
                            postSaveDeviceDataViewModel.loadData(SPO2,HR, Temp, COUGH,
                                BleCache.mDeviceInfo?.mBleAddress!!, _activity, Helper.getCurrentDate().toString())
                        }
                        Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
                    }
                } catch (e: java.lang.Exception) {
                    // TODO: handle exception
                } finally {
                    //also call the same runnable to call it at regular interval
                    handler.postDelayed(this, 100000)
                }
            }
        }
        handler.postDelayed(runnable, 100000)
    }

    fun setSaveDeviceDataAPIObserver() {
        postSaveDeviceDataViewModel = ViewModelProviders.of(this).get(PostSaveDeviceDataViewModel::class.java).apply {
            this@BaseFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->

                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet as Observer<in Boolean>)
                getTrigger().observe(thisFragReference, Observer {
                    //postGetCovidStatusDataViewModel.loadData(BleCache.mDeviceInfo?.mBleAddress!!)
                })
            }
        }
    }


    fun epcoToDate(date : Int) :String {
        val date = Date(date * 1000L)
        val format: DateFormat = SimpleDateFormat(TIME_JSON_HM)
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"))
        var formatted: String = format.format(date)
        if(formatted.contains(":")) {
            formatted = formatted.replace(":",".")
        }
        return formatted
    }

    companion object {
        var REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 911
    }
}