package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.*
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.TIMEFORMAT
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.fragment_fitness.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*


class FitnessFragment : BaseFragment() {


    private val mBleHandleCallback by lazy {
        object : BleHandleCallback {

            override fun onDeviceConnected(_device: BluetoothDevice) {
                onConnected()
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
               onConnected()
            }

            override fun onReadSleep(sleeps: List<BleSleep>) {
                super.onReadSleep(sleeps)

            }

            override fun onReadHeartRate(heartRates: List<BleHeartRate>) {
                super.onReadHeartRate(heartRates)
                try {
                    heartRateInsert(heartRates)
                } catch (e:java.lang.Exception){

                }
            }

            override fun onReadBloodOxygen(bloodOxygen: List<BleBloodOxygen>) {
                super.onReadBloodOxygen(bloodOxygen)
                try {
                    SpoRateInsert(bloodOxygen)
                } catch (e:java.lang.Exception){

                }
            }

            override fun onReadTemperature(temperatures: List<BleTemperature>) {
                super.onReadTemperature(temperatures)
                try {
                    TempInsert(temperatures)
                } catch (e:java.lang.Exception){

                }
            }

            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                try {
                    syncing_fitness.visibility = View.GONE
                    calories.text = (activities.get(0).mCalorie/10000).toString()
                    val mDistance = (activities.get(0).mDistance/10000).toDouble()
                    val mDist = (mDistance/1000).toDouble()
                    dist.text = String.format("%.2f",mDist)
                    steps.text = (activities.get(0).mStep).toString()
                    if(swiperefresh_items.isRefreshing) {
                        swiperefresh_items.setRefreshing(false);
                    }
                    setData()
                }catch (e:Exception) {
                    e.printStackTrace()
                }
                onConnected()
                insertStepData(activities)

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_fitness, container, false)
        return v;
    }

    fun setData() {
        val stepsArray = lastestHRSteps()
        if(stepsArray!= null && stepsArray?.size != 0) {
            val sync_date = BaseHelper.parseDate(stepsArray?.get(0)?.time,Constants.TIME_JSON_HM)
            last_synced.text = BaseHelper.parseDate(sync_date,TIMEFORMAT)
            calories.text = stepsArray?.get(0)?.total_cal.toString()
            dist.text = String.format("%.2f",stepsArray?.get(0)?.total_dist?.toDouble())
            steps.text = stepsArray?.get(0)?.total_count.toString()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            BleConnector.addHandleCallback(mBleHandleCallback)
            syncing_fitness.visibility = View.VISIBLE
            swiperefresh_items.setOnRefreshListener(OnRefreshListener {
                Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
            })
            welcome.text = "Welcome back, " + UserInfoManager.getInstance(activity!!).getAccountName()
            if (UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
                fitness_human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_female))
            } else {
                fitness_human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_male))
            }
            setData()

        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        refresh.setOnClickListener {
            swiperefresh_items.setRefreshing(true);
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
        }
        if(!BleConnector.isAvailable()) {
            connection_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_circle, 0);

        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            setData()
        }
    }

    fun onConnected() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }
        } catch (e:Exception){

        }
    }

}