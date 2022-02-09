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
import com.yc.pedometer.sdk.DataProcessing
import com.yc.pedometer.sdk.SleepChangeListener
import com.yc.pedometer.sdk.StepChangeListener
import com.yc.pedometer.sdk.UTESQLOperate
import com.yc.pedometer.sdk.WriteCommandToBLE
import com.yc.pedometer.update.Updates
import com.yc.pedometer.utils.CalendarUtils
import com.yc.pedometer.utils.LogUtils
import com.yc.pedometer.utils.SPUtil
import kotlinx.android.synthetic.main.fragment_insight.*



import java.util.*
import kotlin.collections.ArrayList

class InsightsFragment : BaseFragment() ,View.OnClickListener{

    private var mSteps = 0
    private var mDistance = 0f
    private var mCalories = 0f
    private var mRunCalories: kotlin.Float = 0f
    private var mWalkCalories: kotlin.Float = 0f
    private var mRunSteps = 0
    private var mRunDurationTime: Int = 0
    private var mWalkSteps: Int = 0
    private var mWalkDurationTime: Int = 0
    private var mRunDistance = 0f
    private var mWalkDistance: kotlin.Float = 0f
    protected val TAG = "InsightsFragment"

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
        syncing_fitness.visibility = View.VISIBLE
        try {
            lltemp.setOnClickListener(this)
            llsteps.setOnClickListener(this)
            llcal.setOnClickListener(this)
            lldistance.setOnClickListener(this)
            llheartrate.setOnClickListener(this)
            llspo.setOnClickListener(this)



            try {
                setHeartData()
                setSPoData()
                setDistanceData()
                setStepsData()
                setCaloriesData()
                setTempData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mySQLOperate = UTESQLOperate.getInstance(activity) //
            mDataProcessing = DataProcessing.getInstance(mContext)
            mDataProcessing?.setOnStepChangeListener(mOnStepChangeListener)
            mDataProcessing?.setOnSleepChangeListener(mOnSleepChangeListener)

            mWriteCommand = WriteCommandToBLE.getInstance(mContext)
            mUpdates = Updates.getInstance(mContext)
            mWriteCommand?.syncAllStepData()
            activity?.runOnUiThread {
                try {
                    val sleepTimeInfo = UTESQLOperate.getInstance(mContext)
                        .querySleepInfo(CalendarUtils.getCalendar(0))
                    if (sleepTimeInfo != null) {
                        sleep.text = "" + (sleepTimeInfo.sleepTotalTime / 60).toDouble()+" hrs"
                    }
                } catch (e: Exception){

                }
            }

            if (COUGH == 1) {
                cough.text = "Yes"
                cough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
            } else {
                cough.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                cough.text = "NO"

            }

            welcome.text =
                "Welcome back, " + UserInfoManager.getInstance(activity!!).getAccountName()
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }
    val mOnStepChangeListener =
        StepChangeListener { info ->
            if (info != null) {
                mSteps = info.step
                mDistance = info.distance
                mCalories = info.calories
                mRunSteps = info.runSteps
                mRunCalories = info.runCalories
                mRunDistance = info.runDistance
                mRunDurationTime = info.runDurationTime
                mWalkSteps = info.walkSteps
                mWalkCalories = info.walkCalories
                mWalkDistance = info.walkDistance
                mWalkDurationTime = info.walkDurationTime
            }
            LogUtils.d(
                TAG, "mSteps =" + mSteps + ",mDistance ="
                        + mDistance + ",mCalories =" + mCalories + ",mRunSteps ="
                        + mRunSteps + ",mRunCalories =" + mRunCalories
                        + ",mRunDistance =" + mRunDistance + ",mRunDurationTime ="
                        + mRunDurationTime + ",mWalkSteps =" + mWalkSteps
                        + ",mWalkCalories =" + mWalkCalories + ",mWalkDistance ="
                        + mWalkDistance + ",mWalkDurationTime ="
                        + mWalkDurationTime
            )
            activity?.runOnUiThread {
                try {
                    syncing_fitness.visibility = View.GONE
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
                    val lmist = mySQLOperate!!.queryRunWalkAllDay()
                    for(activity in lmist) {
                        activity?.stepOneHourArrayInfo?.let {
                            if(!BaseHelper.isEmpty(activity.calendar)) {
                                insertStepData(
                                    activity.stepOneHourArrayInfo,
                                    activity.calendar,
                                    activity.step.toString(),
                                    String.format("%.2f", activity.calories),
                                    String.format("%.2f", activity.distance)
                                )
                            }
                        }
                    }
                    activity?.runOnUiThread(Runnable {
                        try {
                            setHeartData()
                            setSPoData()
                            setDistanceData()
                            setStepsData()
                            setCaloriesData()
                            setTempData()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    })

                } catch (e: Exception){
                    e.printStackTrace()
                }
            }

        }
    private val mOnSleepChangeListener =
        SleepChangeListener {

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

    fun setCaloriesData() {
        try {
            val diatnceArray = lastestHRSteps()
            if (diatnceArray != null && diatnceArray.size != 0) {
                val cal =  diatnceArray.get(0).total_cal.toDouble()
                calories.text = cal.toInt().toString()

                val parsetime =
                    BaseHelper.parseDate(diatnceArray.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(), Constants.DATE_TIME)
                val lastSyncDateStr = BaseHelper.parseDate(
                    diatnceArray.get(0).date + " " +
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
                if (diff.days.toInt() == 0) {
                    if (diff.hours.toInt() == 0) {
                        cal_last_sync.text = "Current"
                    } else {
                        cal_last_sync.text = Math.abs(diff.hours).toInt().toString() + " hours ago"
                    }
                } else if (diff.days.toInt() == 1) {
                    cal_last_sync.text = "Yesterday"
                } else {
                    cal_last_sync.text = diatnceArray.get(0).date
                }
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

    }

    fun setDistanceData() {
        try {
            val diatnceArray = lastestHRSteps()
            if (diatnceArray != null && diatnceArray.size != 0) {

                distance.text = String.format(
                    "%.2f",
                    diatnceArray.get(0).total_dist.toFloat()
                ) + " km"

                val parsetime = BaseHelper.parseDate(
                    diatnceArray.get(0).time,
                    Constants.TIME_JSON_HM
                )
                val today_date = BaseHelper.parseDate(Date(), Constants.DATE_TIME)
                val dateToParese = diatnceArray.get(0).date + " " + BaseHelper.parseDate(parsetime, Constants.TIME_JSON_HM_)
                val lastSyncDateStr = BaseHelper.parseDate(dateToParese, Constants.DATE_TIME)
                val lastSyncDate = BaseHelper.parseDate(lastSyncDateStr, Constants.DATE_TIME)
                val diff = BaseHelper.printDifference(
                    BaseHelper.parseDate(
                        today_date,
                        Constants.DATE_TIME
                    ), BaseHelper.parseDate(lastSyncDate, Constants.DATE_TIME)
                )
                if (diff.days.toInt() == 0) {
                    if (diff.hours.toInt() == 0) {
                        dist_last_sync.text = "Current"
                    } else {
                        dist_last_sync.text = Math.abs(diff.hours).toInt().toString() + " hours ago"
                    }
                } else if (diff.days.toInt() == 1) {
                    dist_last_sync.text = "Yesterday"
                } else {
                    dist_last_sync.text = diatnceArray.get(0).date
                }
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace();
        }

    }
    fun setStepsData() {
        val db = DataBaseHelper(activity!!)
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
            e.printStackTrace()
        }
        try {

            val _today_date = BaseHelper.parseDate(Date(), Constants.TIME_JSON_HM)
            val sync_date = BaseHelper.parseDate(_today_date,Constants.TIME_JSON_HM)
            last_synced.text = BaseHelper.parseDate(sync_date, TIMEFORMAT)

        }catch (e:Exception){

        }

    }

    fun setHeartData() {
        try {
            val db = DataBaseHelper(activity!!)
            val heartRates = db.getAllHeartRate(" ORDER BY date DESC,time DESC")
            if (heartRates.size != 0) {
                heart_rate.text = heartRates.get(0).heartRate.toString() + " bpm"
                com.watch.aware.app.helper.Constants.HR = heartRates.get(0).heartRate.toInt()
                val parsetime = BaseHelper.parseDate(heartRates.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(), Constants.DATE_TIME)
                val lastSyncDateStr = BaseHelper.parseDate(
                    heartRates.get(0).date + " " +
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
                        heart_last_sync.text = "Current"
                    } else {
                        heart_last_sync.text =
                            Math.abs(diff.hours).toInt().toString() + " hours ago"
                    }
                } else if (diff.days.toInt() == 1) {
                    heart_last_sync.text = "Yesterday"
                } else {
                    heart_last_sync.text = heartRates.get(0).date
                }
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }
    fun setTempData() {
        try {
            val db = DataBaseHelper(activity!!)
            val tempRates = db.getAllTemp(" ORDER BY date DESC,time DESC")
            if (tempRates.size != 0) {
                temp.text = String.format("%.1f", tempRates.get(0).tempRate) + " C"
                com.watch.aware.app.helper.Constants.Temp = tempRates.get(0).tempRate.toDouble()

                val parsetime = BaseHelper.parseDate(tempRates.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(), Constants.DATE_TIME)
                val lastSyncDateStr = BaseHelper.parseDate(
                    tempRates.get(0).date + " " +
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
                if (diff.days.toInt() == 0) {
                    if (diff.hours.toInt() == 0) {
                        temp_last_sync.text = "Current"
                    } else {
                        temp_last_sync.text = Math.abs(diff.hours).toInt().toString() + " hours ago"
                    }
                } else if (diff.days.toInt() == 1) {
                    temp_last_sync.text = "Yesterday"
                } else {
                    temp_last_sync.text = tempRates.get(0).date
                }
            }
        }catch (e:Exception){

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