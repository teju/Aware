package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.amitshekhar.utils.DatabaseHelper
import com.iapps.libs.helpers.BaseHelper
import com.iapps.libs.objects.LastSyncDate
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_JSON_HM
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_hM
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.*
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.COUGH
import com.watch.aware.app.helper.Constants.Companion.HR
import com.watch.aware.app.helper.Constants.Companion.SPO2
import com.watch.aware.app.helper.Constants.Companion.TIMEFORMAT
import com.watch.aware.app.helper.Constants.Companion.TWELVE_HOUR_FORMAT
import com.watch.aware.app.helper.Constants.Companion.Temp
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import com.watch.aware.app.models.HeartRate
import com.watch.aware.app.models.SpoRate
import com.watch.aware.app.webservices.PostCovidStatusDataViewModel
import com.watch.aware.app.webservices.PostRegisterViewModel
import com.watch.aware.app.webservices.PostSaveDeviceDataViewModel
import com.watch.aware.app.webservices.PostUpdateProfileModel
import kotlinx.android.synthetic.main.fragment_welness.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class WelnessFragment : BaseFragment() {
    private var heartlastsynced: Date? = null
    private var spolastsynced: Date? = null
    private var heartRates: List<HeartRate> = ArrayList()
    private var spoRates: List<SpoRate> = ArrayList()
    lateinit var postGetCovidStatusDataViewModel: PostCovidStatusDataViewModel
    lateinit var postUpdateProfileModel: PostUpdateProfileModel

    var diffDaysSpo: LastSyncDate? = null
    var diffHeartRate : LastSyncDate? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_welness, container, false)
        return v;
    }

    val mBleHandleCallback by lazy {
        object : BleHandleCallback {

            override fun onDeviceConnected(_device: BluetoothDevice) {

                onConnected()
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
                onConnected()
            }

            override fun onReadHeartRate(heartRates: List<BleHeartRate>) {
                try {
                    setHeartData()
                    onConnected()
                } catch (e:Exception){
                    e.printStackTrace()
                }
                heartRateInsert(heartRates)

            }

            override fun onReadTemperature(temperatures: List<BleTemperature>) {
                try {
                   setTempData()
                    onConnected()
                } catch (e:Exception){
                    e.printStackTrace()
                }
                TempInsert(temperatures)
            }

            override fun onReadBloodOxygen(bloodOxygen: List<BleBloodOxygen>) {
                try {
                   setSPoData()
                    onConnected()
                } catch (e:Exception){
                    e.printStackTrace()
                }
                SpoRateInsert(bloodOxygen)
            }

            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                insertStepData(activities)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGetCovidStatusDataAPIObserver()
        setUpdateProfileAPIObserver()
        syncing.visibility = View.VISIBLE

        BleConnector.addHandleCallback(mBleHandleCallback)
        var deviceAddress = ""
        if(BleCache.mDeviceInfo != null) {
            deviceAddress = BleCache.mDeviceInfo?.mBleAddress!!
            onConnected()
        }
        if( UserInfoManager.getInstance(activity!!).getISFirstTime()) {
            postUpdateProfileModel.loadData(
                UserInfoManager.getInstance(activity!!).getAccountName(),
                UserInfoManager.getInstance(activity!!).getAge(),
                UserInfoManager.getInstance(activity!!).getEmail(),
                UserInfoManager.getInstance(activity!!).getContactNumber(),
                UserInfoManager.getInstance(activity!!).getGEnder(),
                deviceAddress
            )

        }

        runTimer()
        swiperefresh_items.setOnRefreshListener(OnRefreshListener {
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
            onConnected()

        })
        welcome.text = "Welcome back, "+UserInfoManager.getInstance(activity!!).getAccountName()
        if(UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
            human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_female))
        } else{
            human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_male))
        }
        setTempData()
        setSPoData()
        setHeartData()
        refresh.setOnClickListener {
            swiperefresh_items.setRefreshing(true);
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            BleConnector.addHandleCallback(mBleHandleCallback)
            if(BleCache.mDeviceInfo != null) {
                onConnected()
            }
            if(!BleConnector.isAvailable()) {
                connection_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_circle, 0);
            }
        }

    }

    fun onConnected() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }
            if(!BleConnector.isAvailable()) {
                connection_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_circle, 0);
            } else {
                if (heartRates.size != 0 && spoRates.size != 0) {
                    postGetCovidStatusDataViewModel.loadData(BleCache.mDeviceInfo?.mBleAddress!!)
                } else{
                    info_txt.text = "Please wait, data being transferred ..."
                    info_txt.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
                }
            }
        syncing.visibility = View.GONE
            if(COUGH == 1) {
                tvcough.setText("Yes")
                tvcough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
            } else {
                tvcough.setText("NO")
                tvcough.setTextColor(activity?.resources?.getColor(R.color.Black)!!)
            }
        } catch (e:Exception){
            e.toString()
        }
    }

    fun setHeartData() {
        val db = DataBaseHelper(activity!!)
        heartRates = db.getAllHeartRate("Where heartRate != 0  ORDER by Id DESC")
        if(heartRates.size != 0) {
            try {
                heartlastsynced = BaseHelper.parseDate(heartRates.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(),Constants.DATE_JSON)
                diffHeartRate = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_JSON),
                    BaseHelper.parseDate(heartRates.get(0).date,Constants.DATE_JSON))
                if(diffDaysSpo == null) {
                    if(diffHeartRate?.days?.toInt() == 0) {
                        if (BaseHelper.parseDate(heartlastsynced, TIME_JSON_HM).toDouble() >
                            BaseHelper.parseDate(spolastsynced, TIME_JSON_HM).toDouble()) {
                            last_synced.text = BaseHelper.parseDate(heartlastsynced, TIMEFORMAT)
                        }
                    } else if(diffHeartRate?.days?.toInt() == 1) {
                        last_synced.text = "Yesterday"
                    }
                    else {
                        last_synced.text = heartRates.get(0).date
                    }
                } else if(diffHeartRate?.days?.toInt()!! <= diffDaysSpo?.days?.toInt()!!) {
                    if(diffHeartRate?.days?.toInt() == 0) {
                        if (BaseHelper.parseDate(heartlastsynced, TIME_JSON_HM).toDouble() >
                            BaseHelper.parseDate(spolastsynced, TIME_JSON_HM).toDouble()) {
                            last_synced.text = BaseHelper.parseDate(heartlastsynced, TIMEFORMAT)
                        }
                    } else if(diffHeartRate?.days?.toInt() == 1) {
                        last_synced.text = "Yesterday"
                    }
                    else {
                        last_synced.text = heartRates.get(0).date
                    }
                } else {
                    last_synced.text = BaseHelper.parseDate(spolastsynced, TIMEFORMAT)
                }
            }catch (e:Exception) {
                last_synced.text = BaseHelper.parseDate(heartlastsynced, TIMEFORMAT)
            }
            heart_rate.text = heartRates.get(0).heartRate.toString()
            HR = heartRates.get(0).heartRate.toInt()
        }
    }

    fun setTempData() {
        val db = DataBaseHelper(activity!!)
        val tempRates = db.getAllTemp("Where TempRate != 0 ORDER by Id DESC")
        if(tempRates.size != 0) {
            temp.text = String.format("%.1f",tempRates.get(0).tempRate)
            Temp = tempRates.get(0).tempRate.toDouble()
        }
    }

    fun setSPoData() {
        val db = DataBaseHelper(activity!!)
        spoRates = db.getAllSpoRate("Where SpoRate != 0 ORDER BY Id DESC")
        if(spoRates.size != 0) {
            try {
                spolastsynced = BaseHelper.parseDate(spoRates.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(),Constants.DATE_JSON)
                diffDaysSpo = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_JSON),
                    BaseHelper.parseDate(spoRates.get(0).date,Constants.DATE_JSON))
                if(diffHeartRate == null) {
                    if (diffDaysSpo?.days?.toInt() == 0) {
                        if (heartlastsynced != null && BaseHelper.parseDate(heartlastsynced, Constants.TIME_JSON_HM)
                                .toDouble() <
                            BaseHelper.parseDate(spolastsynced, Constants.TIME_JSON_HM).toDouble()
                        ) {
                            last_synced.text =
                                BaseHelper.parseDate(spolastsynced, TIMEFORMAT)
                        }
                    } else if (diffDaysSpo?.days?.toInt() == 1) {
                        last_synced.text = "Yesterday"
                    } else {
                        last_synced.text = spoRates.get(0).date
                    }

                } else
                if(diffHeartRate?.days?.toInt()!! >= diffDaysSpo?.days?.toInt()!!) {
                    if (diffDaysSpo?.days?.toInt() == 0) {
                        if (BaseHelper.parseDate(heartlastsynced, Constants.TIME_JSON_HM)
                                .toDouble() <
                            BaseHelper.parseDate(spolastsynced, Constants.TIME_JSON_HM).toDouble()
                        ) {
                            last_synced.text =
                                BaseHelper.parseDate(spolastsynced, TIMEFORMAT)
                        }
                    } else if (diffDaysSpo?.days?.toInt() == 1) {
                        last_synced.text = "Yesterday"
                    } else {
                        last_synced.text = spoRates.get(0).date
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
                last_synced.text = BaseHelper.parseDate(spolastsynced, TIMEFORMAT)
            }
            oxygen_level.text = spoRates.get(0).spoRate.toString()
            SPO2 = spoRates.get(0).spoRate
        }
    }



    fun setGetCovidStatusDataAPIObserver() {
        postGetCovidStatusDataViewModel = ViewModelProviders.of(this).get(PostCovidStatusDataViewModel::class.java).apply {
            this@WelnessFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        syncing.visibility = View.VISIBLE
                    } else {
                        syncing.visibility = View.GONE
                    }
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
                getTrigger().observe(thisFragReference, Observer { state ->

                    when (state) {
                        PostCovidStatusDataViewModel.NEXT_STEP -> {
                            when(postGetCovidStatusDataViewModel.obj?.CovidPrediction) {
                                "G" ->{
                                    info_txt.text = "Your wellness data\nseems ok !"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                                "Y" -> {
                                    info_txt.text = "There is some issue\nwith your wellness data"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.DarkOrange)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.DarkOrange), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                                "R" -> {
                                    info_txt.text = "Please contact\nyour doctor"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.Red), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                            }
                        }
                    }
                })

            }
        }
    }

    fun setUpdateProfileAPIObserver() {
        postUpdateProfileModel = ViewModelProviders.of(this).get(PostUpdateProfileModel::class.java).apply {
           this@WelnessFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
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
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostUpdateProfileModel.NEXT_STEP -> {
                            UserInfoManager.getInstance(activity!!).saveIsFirstTime(false)

                        }
                        PostRegisterViewModel.ERROR -> {
                        }
                    }
                })
            }
        }
    }

}