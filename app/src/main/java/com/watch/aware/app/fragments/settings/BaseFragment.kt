package com.watch.aware.app.fragments.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.entity.BleActivity
import com.watch.aware.app.MainActivity
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.callback.PermissionListener
import com.watch.aware.app.fragments.dialog.NotifyDialogFragment
import com.watch.aware.app.helper.Constants.Companion.TIME_JSON_HM
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.Helper.isEmpty
import com.watch.aware.app.models.BaseParams
import com.watch.aware.app.models.Steps
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class BaseFragment : GenericFragment() {

    var permissionsThatNeedTobeCheck: List<String> =
        ArrayList()
    var permissionListener: PermissionListener? = null
    var v: View? = null
    var baseParams = BaseParams()
    open fun onBackTriggered() {
        home()!!.proceedDoOnBackPressed()
    }

    public var cough = 0
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

    fun insertStepData(activities: List<BleActivity>) {
        val dataBaseHelper = DataBaseHelper(activity)
        for(a in activities) {
            val lastHRSteps = lastHRSteps(epcoToDate(a.mTime))
            if(lastHRSteps != null && lastHRSteps.size != 0) {
                val dist : Int = (a.mDistance / 10000).toInt()  - lastHRSteps.get(0).total_dist.trim().toInt()
                val cal : Int = (a.mCalorie / 10000).toInt()  - lastHRSteps.get(0).total_cal.toInt()
                dataBaseHelper.stepsInsert(
                    dataBaseHelper,
                    (a.mStep - lastHRSteps.get(0).total_count.toInt()).toString(),
                    BaseHelper.parseDate(Date(), Constants.DATE_JSON),
                    (dist.toInt()).toString(),
                    (cal.toInt()).toString(),
                    epcoToDate(a.mTime), activities[0].mStep,activities[0].mDistance/10000,activities[0].mCalorie/10000)
            } else {
                dataBaseHelper.stepsInsert(
                    dataBaseHelper,
                    a.mStep.toString(),
                    BaseHelper.parseDate(Date(), Constants.DATE_JSON),
                    ((a.mDistance / 10000)).toString(),
                    ((a.mCalorie / 10000)).toString(),
                    epcoToDate(a.mTime), activities[0].mStep,activities[0].mDistance/10000,activities[0].mCalorie/10000)
            }
        }
    }

    fun lastHRSteps(mTime: String): List<Steps>? {
        val dataBaseHelper = DataBaseHelper(activity)
        val dteps = dataBaseHelper.getAllSteps("WHERE date is  ('" + BaseHelper.parseDate(Date(), Constants.DATE_JSON) + "') " +
                "AND total_count != 0  AND time != '"+mTime+"' ORDER BY total_count DESC LIMIT 1")
        return dteps
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