package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
import com.anychart.core.ui.ChartCredits
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_JSON_HM
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_hM
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
import kotlinx.android.synthetic.main.fragment_goal_progress.*
import java.util.*
import kotlin.collections.ArrayList


class GoalProgressFragment : BaseFragment() {

    fun setBottomNavigation(bottomNavigation: BottomNavigationView?) {
        this.bottomNavigation = bottomNavigation
    }
    private var bottomNavigation: BottomNavigationView? = null
    val data: MutableList<DataEntry> = ArrayList()
    private var cartesian: Cartesian? = null
    private var column: Column? = null
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
                try {
                    steps.text = (activities.get(0).mStep).toString()
                } catch (e:java.lang.Exception){

                }


            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_goal_progress, container, false)
        return v;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BleConnector.addHandleCallback(mBleHandleCallback)
        connect()
        swiperefresh_items.setOnRefreshListener(OnRefreshListener {
           connect()
            getEntries()
            cartesian?.removeAllSeries()
            column = cartesian?.column(data)
        })
    }
    fun connect() {
        if(BleCache.mDeviceInfo != null) {
           onConnected()
        }
    }

    fun onConnected() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }

            getEntries()
            cartesian = AnyChart.column()
            val credits: ChartCredits = cartesian?.credits()!!

            column = cartesian?.column(data)
            column?.labels(true)
            column?.labels()?.fontColor("#78B8F1");
            column?.labels()?.fontWeight(900);

            cartesian?.yScale()?.minimum(0.0)
            cartesian?.yScale()?.maximum(10000.0)
            cartesian?.yScale()?.ticks()?.interval(2000)
            cartesian?.background()?.fill("trans");
            cartesian?.dataArea()?.background()?.enabled(true);
            cartesian?.dataArea()?.background()?.fill("#000000");

            credits.enabled(false)
            credits.text("Custom text");
            anyChartView.setChart(cartesian)
        }catch (e:Exception){

        }
        Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)

    }
    private fun getEntries() {
        data.clear()

            data.add(ValueDataEntry("12 am", isValideData(0.0,2.9)))
            data.add(ValueDataEntry("3 am", isValideData(3.0,5.9) ))
            data.add(ValueDataEntry("6 am", isValideData(6.0,8.9)))
            data.add(ValueDataEntry("9 am", isValideData(9.0,11.9)))
            data.add(ValueDataEntry("12 pm", isValideData(12.0,14.9)))
            data.add(ValueDataEntry("3 pm", isValideData(15.0,17.9)))
            data.add(ValueDataEntry("6 pm", isValideData(18.0,20.9)))
            data.add(ValueDataEntry("9 pm", isValideData(21.0,23.9)))


    }
    fun isValideData(fromnumber : Double,toNumber : Double) :Double {

        val dataBaseHelper = DataBaseHelper(activity!!)
        val query = dataBaseHelper.getAllSteps("ORDER BY stepsCount DESC")
        val lastHour =  BaseHelper.parseDate(query.get(0).time,TIME_JSON_HM)
        last_active_hr.text = BaseHelper.parseDate(lastHour,TIME_hM)
        average_steps.text = (query.get(0).stepCount.toInt()/query.get(0).time.toDouble()).toInt().toString() + " Steps/hr"
        var stepsCnt = 0.0
        try {
            for (i in fromnumber.toInt()..toNumber.toInt()) {
               // val dteps = dataBaseHelper.getAllSteps("WHERE  time >= "+i+" AND  time < "+(i + 1)+" ORDER BY stepsCount DESC" )
                val dteps = dataBaseHelper.getAllSteps("WHERE  date is DATE('"+ BaseHelper.parseDate(
                    Date(), Constants.DATE_JSON)+"') AND time BETWEEN CAST ('"+i+"' as decimal) AND CAST ('"+(i + 0.9)+"' " +
                        "as decimal) ORDER BY stepsCount DESC" )
                if (dteps.size > 0) {
                    for (print in dteps){
                        System.out.println("isValideData "+ print.stepCount.toInt() +" "+ print.time.toDouble())
                    }

                    stepsCnt = stepsCnt + dteps[0].stepCount.toInt()

                }
            }
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }

        return  stepsCnt
    }
}