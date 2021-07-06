package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bestmafen.baseble.scanner.BleDevice
import com.bestmafen.baseble.scanner.BleScanCallback
import com.bestmafen.baseble.scanner.ScannerFactory
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.*
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import com.watch.aware.app.webservices.PostCovidStatusDataViewModel
import com.watch.aware.app.webservices.PostSaveDeviceDataViewModel
import kotlinx.android.synthetic.main.fragment_welness.*
import java.lang.Exception
import java.util.*


class WelnessFragment : BaseFragment() {
    lateinit var postSaveDeviceDataViewModel: PostSaveDeviceDataViewModel
    lateinit var postGetCovidStatusDataViewModel: PostCovidStatusDataViewModel


    var SPO2 = "0"
    var HR = "0"
    var Temp = "0"
    var _activity = "68"

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
                    last_synced.text = BaseHelper.parseDate(Date(), Constants.TIME_hMA)
                    heart_rate.text = heartRates.get(0).mBpm.toString()
                    HR = heartRates.get(0).mBpm.toString()
                    postSaveDeviceDataViewModel.loadData(SPO2,HR,Temp,cough.toString(),
                        BleCache.mDeviceInfo?.mBleAddress!!,_activity, Helper.getCurrentDate().toString())

                } catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onReadTemperature(temperatures: List<BleTemperature>) {
                try {
                    temp.text = temperatures.get(0).mTemperature.toString()
                    Temp = temperatures.get(0).mTemperature.toString()
                    postSaveDeviceDataViewModel.loadData(SPO2,HR,Temp,cough.toString(),
                        BleCache.mDeviceInfo?.mBleAddress!!,_activity, Helper.getCurrentDate().toString())
                } catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onReadBloodOxygen(bloodOxygen: List<BleBloodOxygen>) {
                try {
                    oxygen_level.text = bloodOxygen.get(0).mValue.toString()
                    SPO2 = bloodOxygen.get(0).mValue.toString()
                    postSaveDeviceDataViewModel.loadData(SPO2,HR,Temp,cough.toString(),
                        BleCache.mDeviceInfo?.mBleAddress!!,_activity, Helper.getCurrentDate().toString())
                } catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                insertStepData(activities)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSaveDeviceDataAPIObserver()
        setGetCovidStatusDataAPIObserver()
        syncing.visibility = View.VISIBLE
        BleConnector.addHandleCallback(mBleHandleCallback)
        if(BleCache.mDeviceInfo != null) {
            onConnected()
        }
        swiperefresh_items.setOnRefreshListener(OnRefreshListener {
            if(BleCache.mDeviceInfo != null) {
                onConnected()
            }
        })
        welcome.text = "Welcome back, "+UserInfoManager.getInstance(activity!!).getAccountName()
        if(UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
            //human.setImageDrawable(activity?.resources.getDrawable(R.drawable.human_male))
        } else{
            human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_male))

        }


    }


    fun onConnected() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }
        if(BleCache.mDeviceInfo?.mBleName != null){
            postSaveDeviceDataViewModel.loadData(SPO2,HR,Temp,cough.toString(),BleCache.mDeviceInfo?.mBleAddress!!,_activity, Helper.getCurrentDate().toString())
        }
        syncing.visibility = View.GONE
        } catch (e:Exception){

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
                    last_synced.text = BaseHelper.parseDate(Date(), Constants.TIME_hMA)

                    when (state) {
                        PostCovidStatusDataViewModel.NEXT_STEP -> {
                            when(postGetCovidStatusDataViewModel.obj?.CovidPrediction) {
                                "G" ->{
                                    info_txt.text = "Your wellness data\nseems ok !"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                                    tvcough.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                                "Y" -> {
                                    info_txt.text = "There is some issue\nwith your wellness data"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.DarkOrange)!!)
                                    tvcough.setTextColor(activity?.resources?.getColor(R.color.DarkOrange)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.DarkOrange), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                                "R" -> {
                                    info_txt.text = "Please contact\nyour doctor"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
                                    tvcough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.Red), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                            }
                        }
                    }
                })

            }
        }
    }

    fun setSaveDeviceDataAPIObserver() {
        postSaveDeviceDataViewModel = ViewModelProviders.of(this).get(PostSaveDeviceDataViewModel::class.java).apply {
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
                getTrigger().observe(thisFragReference, Observer {
                    postGetCovidStatusDataViewModel.loadData(BleCache.mDeviceInfo?.mBleAddress!!)
                })
            }
        }
    }
}