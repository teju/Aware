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
import com.bestmafen.baseble.scanner.BleDevice
import com.bestmafen.baseble.scanner.BleScanCallback
import com.bestmafen.baseble.scanner.ScannerFactory
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
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
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.COUGH
import com.watch.aware.app.helper.Constants.Companion.TIMEFORMAT
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.fragment_insight.*


import java.util.*
import kotlin.collections.ArrayList

class InsightsFragment : BaseFragment() {


    private var cartesian: Cartesian? = null
    private var column: Column? = null
    val data: MutableList<DataEntry> = ArrayList()

    val barEntries = ArrayList<BarEntry>()
    var type = "steps"
    var datatype = "week"
    var colour = "#78B8F1"
    private val mBleHandleCallback by lazy {
        object : BleHandleCallback {

            override fun onDeviceConnected(_device: BluetoothDevice) {
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
            }

            override fun onReadHeartRate(heartRates: List<BleHeartRate>) {
                super.onReadHeartRate(heartRates)
                try {
                    heartRateInsert(heartRates)
                    setHeartData()
                } catch (e:java.lang.Exception){ }
            }

            override fun onReadBloodOxygen(bloodOxygen: List<BleBloodOxygen>) {
                super.onReadBloodOxygen(bloodOxygen)
                try {
                    SpoRateInsert(bloodOxygen)
                    setSPoData()
                } catch (e:java.lang.Exception){ }
            }

            override fun onReadTemperature(temperatures: List<BleTemperature>) {
                super.onReadTemperature(temperatures)
                try {
                    TempInsert(temperatures)
                    setTempData()
                } catch (e:java.lang.Exception){ }
            }

            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                try {
                    insertStepData(activities)
                    setDistanceData()
                    setStepsData()
                    setCaloriesData()
                } catch (e:java.lang.Exception){

                }
                connect()
            }
        }
    }

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

        BleConnector.addHandleCallback(mBleHandleCallback)

        swiperefresh_items.setOnRefreshListener(OnRefreshListener {

            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
            connect()

        })
        refresh.setOnClickListener {
            swiperefresh_items.setRefreshing(true);
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
            connect()
        }
        if(!BleConnector.isAvailable()) {
            connection_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_circle, 0);

        }
        try {
            setHeartData()
            setSPoData()
            setDistanceData()
            setStepsData()
            setCaloriesData()
            setTempData()
        }catch (e : Exception){

        }
        if(COUGH == 1) {
            cough.text = "Yes"
            cough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
        } else {
            cough.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
            cough.text = "NO"

        }

        connect()
        welcome.text = "Welcome back, "+ UserInfoManager.getInstance(activity!!).getAccountName()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            try {
                setHeartData()
                setSPoData()
                setDistanceData()
                setStepsData()
                setCaloriesData()
                setTempData()
            }catch (e : Exception){

            }
            if(COUGH == 1) {
                cough.text = "Yes"
                cough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
            } else {
                cough.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                cough.text = "NO"

            }

        }
    }

    fun connect() {
        try {
            swiperefresh_items.setRefreshing(false);

            if (BleCache.mDeviceInfo != null) {
                Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ, activity!!)
            }
        }catch (e:Exception){

        }
    }
    fun setCaloriesData() {
        val db = DataBaseHelper(activity!!)
        val diatnceArray = db.getAllSteps("Where cal > 0 ORDER by time DESC LIMIT 1")
        if(diatnceArray != null && diatnceArray.size != 0) {
            calories.text = diatnceArray.get(0).total_cal.toString()

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
        val diatnceArray = db.getAllSteps("Where distance != 0 ORDER by time DESC LIMIT 1")
        if(diatnceArray != null && diatnceArray.size != 0) {
            distance.text = diatnceArray.get(0).total_dist.toString() +" km"

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
        try {
            val stepsArray = db.getAllSteps("Where stepsCount > 0 ORDER by time DESC LIMIT 1")
            if (stepsArray != null && stepsArray.size != 0) {
                stepsCount.text = stepsArray.get(0).total_count.toString()

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
            last_synced.text = BaseHelper.parseDate(lastSync, TIMEFORMAT)
        }catch (e:Exception){

        }

    }

    fun setHeartData() {
        val db = DataBaseHelper(activity!!)
        val heartRates = db.getAllHeartRate("Where heartRate != 0 ORDER by Id DESC LIMIT 1")
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
        val tempRates = db.getAllTemp("Where TempRate != 0 ORDER by Id DESC LIMIT 1")
        if(tempRates.size != 0) {
            temp.text = String.format("%.1f",tempRates.get(0).tempRate)+" C"
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


    private fun getWeeklyEntries() {
        data.clear()

        data.add(ValueDataEntry("Sun", isValideDataWeek(0)))
        data.add(ValueDataEntry("Mon", isValideDataWeek(1)))
        data.add(ValueDataEntry("Tue", isValideDataWeek(2)))
        data.add(ValueDataEntry("Wed", isValideDataWeek(3)))
        data.add(ValueDataEntry("Thur", isValideDataWeek(4)))
        data.add(ValueDataEntry("Fri", isValideDataWeek(5)))
        data.add(ValueDataEntry("Sat", isValideDataWeek(6)))


    }
    private fun getYearlyEntries() {
        data.clear()
        data.add(ValueDataEntry("2021", isValideDataYearly(2021) ))
    }

    private fun getMonthlyEntries() {
        data.clear()

        data.add(ValueDataEntry("Jan", isValideDataWeekMonthly(1)))
        data.add(ValueDataEntry("Feb",  isValideDataWeekMonthly(2)))
        data.add(ValueDataEntry("Mar",  isValideDataWeekMonthly(3)))
        data.add(ValueDataEntry("Apr",  isValideDataWeekMonthly(4)))
        data.add(ValueDataEntry("May",  isValideDataWeekMonthly(5)))
        data.add(ValueDataEntry("June",  isValideDataWeekMonthly(6)))
        data.add(ValueDataEntry("July", isValideDataWeekMonthly(7)))
        data.add(ValueDataEntry("Aug",  isValideDataWeekMonthly(8)))
        data.add(ValueDataEntry("Sept", isValideDataWeekMonthly(9)))
        data.add(ValueDataEntry("Oct",  isValideDataWeekMonthly(10)))
        data.add(ValueDataEntry("Nov",  isValideDataWeekMonthly(11)))
        data.add(ValueDataEntry("Dex",  isValideDataWeekMonthly(12)))


    }
    fun isValideDataWeek(day : Int) :Int {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var stepsCnt = 0
        val dteps = dataBaseHelper.getAllStepsWeekly(day)
        if(dteps!= null && dteps.size > 0) {

            when (type) {
                "dist" -> {
                    stepsCnt = stepsCnt + dteps.get(0).distance.toInt()
                }
                "steps" -> {
                    stepsCnt = stepsCnt + dteps.get(0).total_count.toInt()

                }
                "cal" -> {
                    stepsCnt = stepsCnt + dteps.get(0).cal.toInt()
                }
            }

        }
        return  stepsCnt
    }
    fun isValideDataYearly(day : Int) :Int {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var stepsCnt = 0
        val dteps = dataBaseHelper.getAllStepsYearly(day)
        for (steps in dteps ) {
            when(type) {
                "dist" -> {
                    stepsCnt = stepsCnt + steps.distance.toInt()
                }
                "steps" -> {
                    stepsCnt = stepsCnt + steps.total_count.toInt()

                }
                "cal" -> {
                    stepsCnt = stepsCnt + steps.cal.toInt()
                }
            }
        }
        return  stepsCnt
    }

    fun isValideDataWeekMonthly(day : Int) :Int {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var stepsCnt = 0
        val dteps = dataBaseHelper.getAllStepsMonthly(day)
        for (steps in dteps ) {
            when(type) {
                "dist" -> {
                    stepsCnt = stepsCnt + steps.distance.toInt()
                }
                "steps" -> {
                    stepsCnt = stepsCnt + steps.total_count.toInt()

                }
                "cal" -> {
                    stepsCnt = stepsCnt + steps.cal.toInt()
                }
            }
        }
        return  stepsCnt
    }

/*
    fun selectDataType() {
        year.setOnClickListener {
            year.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            month.setBackgroundColor(resources.getColor(R.color.black))
            week.setBackgroundColor(resources.getColor(R.color.black))
            datatype = "year"
            getYearlyEntries()
            resetData()

        }
        month.setOnClickListener {
            year.setBackgroundColor(resources.getColor(R.color.black))
            month.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            week.setBackgroundColor(resources.getColor(R.color.black))
            datatype = "month"
            getMonthlyEntries()
            resetData()

        }
        week.setOnClickListener {
            year.setBackgroundColor(resources.getColor(R.color.black))
            month.setBackgroundColor(resources.getColor(R.color.black))
            week.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            datatype = "week"
            getWeeklyEntries()
            resetData()

        }
    }
*/

/*
    fun checkBoxFun() {

        distnce.setOnClickListener {
            steps.isChecked = false
            calories.isChecked = false
            type = "dist"
            colour = "#4CAF50"
            when(datatype) {
                "week" -> {
                    getWeeklyEntries()
                }
                "month" -> {
                    getMonthlyEntries()
                }
                "year" -> {
                    getYearlyEntries()
                }
            }
            resetData()


        }
        steps.setOnClickListener {
            distnce.isChecked = false
            calories.isChecked = false
            type = "steps"
            colour = "#2196F3"
            when(datatype) {
                "week" -> {
                    getWeeklyEntries()
                }
                "month" -> {
                    getMonthlyEntries()
                }
                "year" -> {
                    getYearlyEntries()
                }
            }
            resetData()

        }
        calories.setOnClickListener {
            steps.isChecked = false
            distnce.isChecked = false
            type = "cal"
            colour = "#FF9800"
            when(datatype) {
                "week" -> {
                    getWeeklyEntries()
                }
                "month" -> {
                    getMonthlyEntries()
                }
                "year" -> {
                    getYearlyEntries()
                }
            }
            resetData()

        }
    }
*/
}