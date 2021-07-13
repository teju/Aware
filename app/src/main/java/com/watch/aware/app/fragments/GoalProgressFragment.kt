package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.iapps.libs.helpers.BaseHelper
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
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.MyMarkerView
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.fragment_goal_progress.*

import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class GoalProgressFragment : BaseFragment() {

    val xAxisValues: List<String> = ArrayList(
        Arrays.asList("",
            "12 am",
            "3 am",
            "6 am",
            "9 am",
            "12 pm",
            "3 pm",
            "6 pm",
            "9 pm" )
    )
    val values: MutableList<Entry> = ArrayList()
    private val mBleHandleCallback by lazy {
        object : BleHandleCallback {

            override fun onDeviceConnected(_device: BluetoothDevice) {
                try {
                    renderData()
                } catch (e : Exception) {

                }
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
                try {
                    renderData()
                } catch (e : Exception) {
                }
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
                    last_synced.text  = BaseHelper.parseDate(Date(), Constants.TIME_hMA)
                    val amount: Double = (activities.get(0).mStep).toDouble()
                    val res = amount / 10000 * 100
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.progress = res.toFloat()
                    }
                    step_count.text = ""+ String.format("%.2f",res) + "%\nof steps taken"
                    average_steps.text = (activities.get(0).mStep/(BaseHelper.parseDate(Date(),Constants.TIME_hA).toInt())).toString()
                } catch (e:java.lang.Exception){
                }
                renderData()
                insertStepData(activities)
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
        swiperefresh_items.run {
            swiperefresh_items.setOnRefreshListener(OnRefreshListener {
                Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
               connect()
            })
        }
        welcome.text = "Welcome back, "+ UserInfoManager.getInstance(activity!!).getAccountName()
        refresh.setOnClickListener {
            swiperefresh_items.setRefreshing(true);
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
        }
    }

    fun connect() {
        if(BleCache.mDeviceInfo != null) {
           renderData()
        }
    }
    fun setAnylasisData() {
        try {
            val dataBaseHelper = DataBaseHelper(activity)
            val dteps = dataBaseHelper.getAllSteps(
                "WHERE date is  ('" + BaseHelper.parseDate(Date(), Constants.DATE_JSON) + "') " +
                        "AND total_count != 0 ORDER by total_count DESC LIMIT 1"
            )
            if (dteps != null && dteps.size > 0) {
                val lasthr = Helper.convertStringToDate(TIME_JSON_HM, dteps.get(0).time)
                last_active_hr.text = BaseHelper.parseDate(lasthr, TIME_hM)
                val avg_steps = (dteps.get(0).total_count.toInt() / (BaseHelper.parseDate(
                    Date(),
                    Constants.TIME_hA
                ).toInt()))
                average_steps.text = avg_steps.toString()
                val sync_date = BaseHelper.parseDate(dteps?.get(0)?.time, Constants.TIME_JSON_HM)
                last_synced.text = BaseHelper.parseDate(sync_date, Constants.TIME_hM)
            }
        } catch (e:Exception) {

        }
    }

    fun renderData() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }

        setAnylasisData()
        mChart.setTouchEnabled(true)
        mChart.setPinchZoom(true)
        val mv = MyMarkerView(activity, R.layout.custom_marker_view)
        mv.setChartView(mChart)
        mChart.marker = mv

        val xAxis: XAxis = mChart.getXAxis()
        xAxis.textColor = Color.BLACK
        xAxis.setDrawLimitLinesBehindData(false)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        val leftAxis: YAxis = mChart.getAxisLeft()
        leftAxis.removeAllLimitLines()

        leftAxis.axisMaximum = 10000f
        leftAxis.axisMinimum = 0f
        leftAxis.textColor = Color.GRAY
        leftAxis.setDrawZeroLine(false)

        mChart.getAxisRight().setEnabled(false)
        mChart.getDescription().setEnabled(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setValueFormatter(IndexAxisValueFormatter(xAxisValues))


        setGraphData()
        } catch (e:Exception){

        }
    }

    private fun setGraphData() {
        values.clear()
        values.add(Entry(1f, getStepCount(0.0,3.0)))
        values.add(Entry(2f, getStepCount(3.0,6.0) ))
        values.add(Entry(3f, getStepCount(6.0,9.0)))
        values.add(Entry(4f, getStepCount(9.0,12.0)))
        values.add(Entry(5f, getStepCount(12.0,15.0)))
        values.add(Entry(6f, getStepCount(15.0,18.0)))
        values.add(Entry(7f, getStepCount(18.0,21.0)))
        values.add(Entry(8f, getStepCount(21.0,23.59)))

        val set1: LineDataSet
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = mChart.getData().getDataSetByIndex(0) as LineDataSet
            set1.values = values
            mChart.getData().notifyDataChanged()
            mChart.notifyDataSetChanged()
        } else {
            set1 = LineDataSet(values, "")
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            set1.setDrawIcons(false)
            set1.color = activity?.resources?.getColor(R.color.colorAccent)!!
            set1.setCircleColor(Color.DKGRAY)
            set1.lineWidth = 2f
            set1.circleRadius = 3f
            set1.setDrawCircleHole(false)
            set1.valueTextSize = 9f
            set1.setDrawFilled(true)
            set1.formLineWidth = 1f
            set1.formSize = 15f
            set1.fillColor = Color.WHITE
            val dataSets: ArrayList<ILineDataSet> = ArrayList()

            dataSets.add(set1)
            val data = LineData(dataSets)
            mChart.setData(data)
        }
    }

    fun getStepCount(fromnumber : Double,toNumber : Double) :Float {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var stepsCnt = 0.0
        try {
            val dteps = dataBaseHelper.getAllSteps("WHERE  date is DATE('"+ BaseHelper.parseDate(
                Date(), Constants.DATE_JSON)+"') AND time >= CAST ('"+fromnumber+"' as decimal) AND  time < CAST ('"+toNumber+"' " +
                    "as decimal) ORDER BY Id DESC" )
            if (dteps.size > 0) {
                for (step in dteps){
                    stepsCnt = stepsCnt + step.stepCount.toInt()
                    System.out.println(" getStepCount "+stepsCnt)
                }
            }
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return  stepsCnt.toFloat()
    }
}