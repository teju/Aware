package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
import com.anychart.core.ui.ChartCredits

import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants

import com.watch.aware.app.R
import com.watch.aware.app.fragments.graphs.*
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.COUGH
import com.watch.aware.app.helper.Constants.Companion.TIMEFORMAT
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import com.yc.pedometer.utils.SPUtil
import kotlinx.android.synthetic.main.fragment_insight.*


import java.util.*
import kotlin.collections.ArrayList

class InsightsFragment : BaseFragment() ,View.OnClickListener{


    private var cartesian: Cartesian? = null
    private var column: Column? = null
    val data: MutableList<DataEntry> = ArrayList()

    val barEntries = ArrayList<BarEntry>()
    var type = "steps"
    var datatype = "week"
    var colour = "#78B8F1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_insight, container, false)
        return v;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            lltemp.setOnClickListener(this)
            llsteps.setOnClickListener(this)
            llcal.setOnClickListener(this)
            lldistance.setOnClickListener(this)
            llheartrate.setOnClickListener(this)
            llspo.setOnClickListener(this)

            swiperefresh_items.setOnRefreshListener(OnRefreshListener {


                connect()

            })
            refresh.setOnClickListener {
                swiperefresh_items.setRefreshing(true);
                connect()
            }
//        if(!BleConnector.isAvailable()) {
//
//
//        }
            val connected = SPUtil.getInstance(activity?.getApplicationContext()).bleConnectStatus
            if(connected) {
                connection_status.setText(getString(R.string.connected))
                connection_status.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.check_circle,
                    0
                );
            }
            try {
                setHeartData()
                setSPoData()
                setDistanceData()
                setStepsData()
                setCaloriesData()
                setTempData()
            } catch (e: Exception) {

            }
            if (COUGH == 1) {
                cough.text = "Yes"
                cough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
            } else {
                cough.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                cough.text = "NO"

            }

            connect()
            welcome.text =
                "Welcome back, " + UserInfoManager.getInstance(activity!!).getAccountName()
        } catch (e:java.lang.Exception){

        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        try {
            if (!hidden) {
                try {
                    setHeartData()
                    setSPoData()
                    setDistanceData()
                    setStepsData()
                    setCaloriesData()
                    setTempData()
                } catch (e: Exception) {

                }
                if (COUGH == 1) {
                    cough.text = "Yes"
                    cough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
                } else {
                    cough.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                    cough.text = "NO"

                }

            }
        }catch (e:java.lang.Exception){

        }
    }

    fun connect() {
        try {
            swiperefresh_items.setRefreshing(false);

        }catch (e:Exception){

        }
    }
    fun setCaloriesData() {
        val db = DataBaseHelper(activity!!)
        val tdate = BaseHelper.parseDate(Date(),Constants.DATE_JSON)

        val diatnceArray = lastestHRSteps()
        if(diatnceArray != null && diatnceArray.size != 0) {
            calories.text = String.format("%.3f", diatnceArray.get(0).total_cal.toFloat())

            val parsetime = BaseHelper.parseDate(diatnceArray.get(0).time,Constants.TIME_JSON_HM)
            val today_date = BaseHelper.parseDate(Date(),Constants.DATE_TIME)
            val lastSyncDateStr = BaseHelper.parseDate(diatnceArray.get(0).date +" "+
                    BaseHelper.parseDate(parsetime,Constants.TIME_JSON_HM_),Constants.DATE_TIME)
            val lastSyncDate = BaseHelper.parseDate(lastSyncDateStr,Constants.DATE_TIME)
            val diff = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_TIME),BaseHelper.parseDate(lastSyncDate,Constants.DATE_TIME))
            System.out.println(" ")
            if(diff.days.toInt() == 0) {
                if(diff.hours.toInt() == 0) {
                    cal_last_sync.text = "Current"
                } else{
                    cal_last_sync.text =  Math.abs(diff.hours).toInt().toString()+ " hours ago"
                }
            } else if(diff.days.toInt() == 1) {
                cal_last_sync.text = "Yesterday"
            } else {
                cal_last_sync.text = diatnceArray.get(0).date
            }
        }

    }

    fun setDistanceData() {
        val db = DataBaseHelper(activity!!)
        val tdate = BaseHelper.parseDate(Date(),Constants.DATE_JSON)

        val diatnceArray = lastestHRSteps()
        if(diatnceArray != null && diatnceArray.size != 0) {

            distance.text = String.format("%.3f", diatnceArray.get(0).total_dist.toFloat())+" km"

            val parsetime = BaseHelper.parseDate(diatnceArray.get(0).time,Constants.TIME_JSON_HM)
            val today_date = BaseHelper.parseDate(Date(),Constants.DATE_TIME)
            val lastSyncDateStr = BaseHelper.parseDate(diatnceArray.get(0).date +" "+
                    BaseHelper.parseDate(parsetime,Constants.TIME_JSON_HM_),Constants.DATE_TIME)
            val lastSyncDate = BaseHelper.parseDate(lastSyncDateStr,Constants.DATE_TIME)
            val diff = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_TIME),BaseHelper.parseDate(lastSyncDate,Constants.DATE_TIME))
            System.out.println(" ")
            if(diff.days.toInt() == 0) {
                if(diff.hours.toInt() == 0) {
                    dist_last_sync.text = "Current"
                } else{
                    dist_last_sync.text =  Math.abs(diff.hours).toInt().toString()+ " hours ago"
                }
            } else if(diff.days.toInt() == 1) {
                dist_last_sync.text = "Yesterday"
            } else {
                dist_last_sync.text = diatnceArray.get(0).date
            }
        }

    }
    fun setStepsData() {
        val db = DataBaseHelper(activity!!)
        val tdate = BaseHelper.parseDate(Date(),Constants.DATE_JSON)
        try {
            val stepsArray = lastestHRSteps()
            if (stepsArray != null && stepsArray.size != 0) {

                stepsCount.text = stepsArray.get(0).total_steps.toString()

                val parsetime = BaseHelper.parseDate(stepsArray.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(), Constants.DATE_TIME)
                val lastSyncDateStr = BaseHelper.parseDate(
                    stepsArray.get(0).date + " " +
                            BaseHelper.parseDate(parsetime, Constants.TIME_JSON_HM_),
                    Constants.DATE_TIME
                )
                val lastSyncDate = BaseHelper.parseDate(lastSyncDateStr, Constants.DATE_TIME)
                val diff = BaseHelper.printDifference(
                    BaseHelper.parseDate(
                        today_date,
                        Constants.DATE_TIME
                    ), BaseHelper.parseDate(lastSyncDate, Constants.DATE_TIME)
                )
                System.out.println(" ")
                if (diff.days.toInt() == 0) {
                    if (diff.hours.toInt() == 0) {
                        steps_last_sync.text = "Current"
                    } else {
                        steps_last_sync.text =
                            Math.abs(diff.hours).toInt().toString() + " hours ago"
                    }
                } else if (diff.days.toInt() == 1) {
                    steps_last_sync.text = "Yesterday"
                } else {
                    steps_last_sync.text = stepsArray.get(0).date
                }
            }
        } catch (e:java.lang.Exception) {

        }
        try {
            val activities = db.getAllSteps(
                "WHERE  " +
                        "date is DATE('" + BaseHelper.parseDate(
                    Date(),
                    Constants.DATE_JSON
                ) + "') AND stepsCount != 0 ORDER BY time DESC"
            )
            val lastSync = BaseHelper.parseDate(activities.get(0).time, Constants.TIME_JSON_HM)
            val _today_date = BaseHelper.parseDate(Date(), Constants.TIME_JSON_HM)
            val sync_date = BaseHelper.parseDate(_today_date,Constants.TIME_JSON_HM)
            if(lastSync > sync_date) {
                last_synced.text = BaseHelper.parseDate(sync_date, TIMEFORMAT)

            } else {
                last_synced.text = BaseHelper.parseDate(lastSync, TIMEFORMAT)

            }
        }catch (e:Exception){

        }

    }

    fun setHeartData() {
        val db = DataBaseHelper(activity!!)
        val heartRates = db.getAllHeartRate(" ORDER BY date DESC,time DESC")
        if(heartRates.size != 0) {
            heart_rate.text = heartRates.get(0).heartRate.toString() +" bpm"
            com.watch.aware.app.helper.Constants.HR = heartRates.get(0).heartRate.toInt()
            val parsetime = BaseHelper.parseDate(heartRates.get(0).time,Constants.TIME_JSON_HM)
            val today_date = BaseHelper.parseDate(Date(),Constants.DATE_TIME)
            val lastSyncDateStr = BaseHelper.parseDate(heartRates.get(0).date +" "+
                    BaseHelper.parseDate(parsetime,Constants.TIME_JSON_HM_),Constants.DATE_TIME)
            val lastSyncDate = BaseHelper.parseDate(lastSyncDateStr,Constants.DATE_TIME)
            val diff = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_TIME),BaseHelper.parseDate(lastSyncDate,Constants.DATE_TIME))
            System.out.println(" ")
            if(diff.days.toInt() == 0) {
                if(diff.hours.toInt() == 0) {
                    heart_last_sync.text = "Current"
                } else{
                    heart_last_sync.text =  Math.abs(diff.hours).toInt().toString()+ " hours ago"
                }
            } else if(diff.days.toInt() == 1) {
                heart_last_sync.text = "Yesterday"
            } else {
                heart_last_sync.text = heartRates.get(0).date
            }
        }
    }
    fun setTempData() {
        val db = DataBaseHelper(activity!!)
        val tempRates = db.getAllTemp(" ORDER BY date DESC,time DESC")
        if(tempRates.size != 0) {
            temp.text = String.format("%.1f",tempRates.get(0).tempRate)+" BPM"
            com.watch.aware.app.helper.Constants.Temp = tempRates.get(0).tempRate.toDouble()

            val parsetime = BaseHelper.parseDate(tempRates.get(0).time,Constants.TIME_JSON_HM)
            val today_date = BaseHelper.parseDate(Date(),Constants.DATE_TIME)
            val lastSyncDateStr = BaseHelper.parseDate(tempRates.get(0).date +" "+
                    BaseHelper.parseDate(parsetime,Constants.TIME_JSON_HM_),Constants.DATE_TIME)
            val lastSyncDate = BaseHelper.parseDate(lastSyncDateStr,Constants.DATE_TIME)
            val diff = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_TIME),BaseHelper.parseDate(lastSyncDate,Constants.DATE_TIME))
            if(diff.days.toInt() == 0) {
                if(diff.hours.toInt() == 0) {
                    temp_last_sync.text = "Current"
                } else{
                    temp_last_sync.text =  Math.abs(diff.hours).toInt().toString()+ " hours ago"
                }
            } else if(diff.days.toInt() == 1) {
                temp_last_sync.text = "Yesterday"
            } else {
                temp_last_sync.text = tempRates.get(0).date
            }
        }
    }

    fun setSPoData() {
        spo_last_sync.text = "Current"
        oxygen_level.text = "96 %"
        val db = DataBaseHelper(activity!!)
        val spoRates = db.getAllSpoRate("Where SpoRate != 0 ORDER BY Id DESC LIMIT 1")
        if(spoRates.size != 0) {
            oxygen_level.text = spoRates.get(0).spoRate.toString()+" %"
            com.watch.aware.app.helper.Constants.SPO2 = spoRates.get(0).spoRate

            val parsetime = BaseHelper.parseDate(spoRates.get(0).time,Constants.TIME_JSON_HM)
            val today_date = BaseHelper.parseDate(Date(),Constants.DATE_TIME)
            val lastSyncDateStr = BaseHelper.parseDate(spoRates.get(0).date +" "+
                    BaseHelper.parseDate(parsetime,Constants.TIME_JSON_HM_),Constants.DATE_TIME)
            val lastSyncDate = BaseHelper.parseDate(lastSyncDateStr,Constants.DATE_TIME)
            val diff = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_TIME),BaseHelper.parseDate(lastSyncDate,Constants.DATE_TIME))
            System.out.println(" ")
            if(diff.days.toInt() == 0) {
                if(diff.hours.toInt() == 0) {
                    spo_last_sync.text = "Current"
                } else{
                    spo_last_sync.text = Math.abs(diff.hours).toInt().toString() + " hours ago"
                }
            } else if(diff.days.toInt() == 1) {
                spo_last_sync.text = "Yesterday"
            } else {
                spo_last_sync.text = spoRates.get(0).date
            }
        }
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.lldistance -> {
                home()?.setFragment(DistanceGraphFragment())
            }
            R.id.llheartrate -> {
                home()?.setFragment(HeartRateGraphFragment())
            }
            R.id.llsteps -> {
                home()?.setFragment(StepsGraphFragment())
            }
            R.id.llspo -> {
                home()?.setFragment(SpoGraphFragment())
            }
            R.id.lltemp -> {
                home()?.setFragment(TempGraphFragment())
            }
            R.id.llcal -> {
                home()?.setFragment(CaloriesGraphFragment())
            }
        }
    }
}