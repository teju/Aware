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
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_JSON_HM
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.*
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.*
import com.watch.aware.app.helper.Constants.Companion.CAL_GOAL
import com.watch.aware.app.helper.Constants.Companion.STEPS_GOAL
import kotlinx.android.synthetic.main.fragment_goal_progress.*
import java.util.*
import kotlin.collections.ArrayList


class GoalProgressFragment : BaseFragment() {

    var xAxisValues: List<String> = ArrayList(
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
    var values: MutableList<Entry> = ArrayList()
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
//                    last_synced.text  = BaseHelper.parseDate(Date(),
//                        com.watch.aware.app.helper.Constants.TIMEFORMAT
//                    )
//                    var totalVal :Double = 0.0
//                    var appendText = ""
//                    if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
//                        totalVal = (activities.get(0).mStep).toDouble()
//                        appendText = "of steps taken"
//                    } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
//                        totalVal = (activities.get(0).mCalorie/10000).toDouble()
//                        appendText = "of calories burnt"
//                    } else {
//                        val mDistance = (activities.get(0).mDistance/10000).toDouble()
//                        totalVal = (mDistance/1000).toDouble()
//                        appendText = "of distance travelled"
//                    }
//
//                    val res = totalVal / UserInfoManager.getInstance(activity!!).getGoalValue() * 100
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        progressBar.progress = res.toFloat()
//                    }
//                    step_count.text = ""+ String.format("%.2f",res) + "%\n"+appendText
//                    average_steps.text = (activities.get(0).mStep/(BaseHelper.parseDate(Date(),Constants.TIME_hA).toInt())).toString()
//
                    setData()
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

    fun setData() {
        val dataBaseHelper = DataBaseHelper(activity!!)

        try {
            val activities = dataBaseHelper.getAllSteps("WHERE  " +
                    "date is DATE('"+ BaseHelper.parseDate(Date(), Constants.DATE_JSON)+"') ORDER BY time DESC")
            var totalVal :Double = 0.0
            var appendText = ""
            if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
                totalVal = (activities.get(0).total_count).toDouble()
                appendText = "of steps taken"
            } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
                totalVal = (activities.get(0).total_cal.toDouble()).toDouble()
                appendText = "of calories burnt"
            } else {
                val mDistance = (activities.get(0).total_dist.toDouble()).toDouble()
                totalVal = (mDistance/1000).toDouble()
                appendText = "of distance travelled"
            }

            val res = totalVal / UserInfoManager.getInstance(activity!!).getGoalValue() * 100
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.progress = res.toFloat()
            }
            step_count.text = ""+ String.format("%.2f",res) + "%\n"+appendText
            average_steps.text = (activities.get(0).total_count.toInt()/(BaseHelper.parseDate(Date(),Constants.TIME_hA).toInt())).toString()
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        try {
            val activities = dataBaseHelper.getAllSteps("WHERE  " +
                    "date is DATE('"+ BaseHelper.parseDate(Date(), Constants.DATE_JSON)+"') AND stepsCount != 0 ORDER BY time DESC")
            if(activities !=null && activities != null && activities.size != 0) {
                val lastSync = BaseHelper.parseDate(activities.get(0).time, Constants.TIME_JSON_HM)
                last_synced.text =
                    BaseHelper.parseDate(lastSync, com.watch.aware.app.helper.Constants.TIMEFORMAT)
            }
        }catch (e:Exception){

        }

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
        if(!BleConnector.isAvailable()) {
            connection_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_circle, 0);

        }
        setData()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            connect()
            setData()
        }
    }
    fun connect() {
        if(BleCache.mDeviceInfo != null) {
           renderData()
            setAnylasisData()
        }
    }
    fun setAnylasisData() {
        val dataBaseHelper = DataBaseHelper(activity)
        try {

            val dteps = dataBaseHelper.getAllSteps(
                "WHERE date is  ('" + BaseHelper.parseDate(Date(), Constants.DATE_JSON) + "') " +
                        "AND total_count != 0 ORDER by total_count DESC LIMIT 1"
            )
            if (dteps != null && dteps.size > 0) {
                val lasthr = Helper.convertStringToDate(TIME_JSON_HM, dteps.get(0).time)
                last_active_hr.text = BaseHelper.parseDate(lasthr, com.watch.aware.app.helper.Constants.TIMEFORMAT)
                val avg_steps = (dteps.get(0).total_count.toInt() / (BaseHelper.parseDate(Date(), Constants.TIME_hA).toInt()))
                average_steps.text = avg_steps.toString()
               // max_step.text = dataBaseHelper.getMaxSteps("").toString()

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

        mChart.setTouchEnabled(false)
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
        leftAxis.axisMaximum = UserInfoManager.getInstance(activity!!).getGoalValue().toFloat()
        if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
            leftAxis.granularity = 2000f
        } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
            leftAxis.granularity = 1000f
        } else {
            leftAxis.granularity = 2f
        }
        leftAxis.axisMinimum = 0f
        leftAxis.setLabelCount(50)
        leftAxis.textColor = Color.GRAY
        leftAxis.setDrawZeroLine(false)

        mChart.getAxisRight().setEnabled(false)
        mChart.getDescription().setEnabled(false);
        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setValueFormatter(IndexAxisValueFormatter(xAxisValues))
        mChart.getLegend().setEnabled(false);


            setGraphData()
        } catch (e:Exception){

        }
    }

    private fun setGraphData() {
        values.clear()
        if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
            values = GoalData().getXAxisStepGoal(activity!!)
        } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
            values = GoalData().getXAxisCAlGoal(activity!!)
        } else {
            values = GoalData().getXAxisDistGoal(activity!!)
        }

        val set1: LineDataSet
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = mChart.getData().getDataSetByIndex(0) as LineDataSet
            set1.values = values
            mChart.getData().notifyDataChanged()
            mChart.notifyDataSetChanged()
        } else {
            set1 = LineDataSet(values, "")
            set1.setDrawValues(true);
            set1.setDrawCircles(false);
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

}