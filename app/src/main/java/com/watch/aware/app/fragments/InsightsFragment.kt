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
import com.szabh.smable3.entity.BleActivity
import com.szabh.smable3.entity.BleDeviceInfo
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
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
                onConnected()
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
                onConnected()
            }


            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)

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
        getWeeklyEntries()
        swiperefresh_items.setOnRefreshListener(OnRefreshListener {
            connect()
            onConnected()
        })

        connect()
        welcome.text = "Welcome back, "+ UserInfoManager.getInstance(activity!!).getAccountName()
        last_synced.text =  BaseHelper.parseDate(Date(), Constants.TIME_hMA)
    }
    fun connect() {
        if(BleCache.mDeviceInfo != null) {
            onConnected()
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)

        }
    }

    fun onConnected() {


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
        if(dteps.size > 0) {

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