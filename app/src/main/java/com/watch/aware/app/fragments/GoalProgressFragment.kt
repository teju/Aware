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

import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.CAL_GOAL
import com.watch.aware.app.helper.Constants.Companion.STEPS_GOAL
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.MyMarkerView
import com.watch.aware.app.helper.UserInfoManager
import com.watch.aware.app.models.DailyData
import com.yc.pedometer.sdk.UTESQLOperate
import com.yc.pedometer.utils.SPUtil
import kotlinx.android.synthetic.main.fragment_goal_progress.*
import java.util.*
import kotlin.collections.ArrayList


class GoalProgressFragment : BaseFragment(),OnChartValueSelectedListener {

    var xAxisValues: List<String> = ArrayList()
    var values: MutableList<Entry> = ArrayList()


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
                    "date is DATE('"+ BaseHelper.parseDate(Date(), Constants.DATE_JSON)+"')")
            var totalVal :Double = 0.0
            var appendText = ""
            if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
                for(a in activities ) {
                    totalVal = totalVal + (a.stepCount).toDouble()
                }
                appendText = "of steps taken"
            } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
                for(a in activities ) {
                    totalVal = totalVal + (a.cal).toDouble()
                }
                appendText = "of calories burnt"
            } else {
                for(a in activities ) {
                    totalVal = totalVal + (a.distance).toDouble()
                }
                appendText = "of distance travelled"
            }

            val res = totalVal / UserInfoManager.getInstance(activity!!).getGoalValue() * 100
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(res.toFloat(),false)
            }

            step_count.text = ""+ res.toInt() + "%\n"+appendText
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        try {
            val today_date = BaseHelper.parseDate(Date(),Constants.TIME_JSON_HM)
            val sync_date = BaseHelper.parseDate(today_date,Constants.TIME_JSON_HM)
            val activities = dataBaseHelper.getAllSteps("WHERE  " +
                    "date is DATE('"+ BaseHelper.parseDate(Date(), Constants.DATE_JSON)+"') ORDER by time DESC")
            if(activities !=null && activities.size != 0) {
                val lastSync = BaseHelper.parseDate(activities.get(0).time, Constants.TIME_JSON_HM)
                if(lastSync > sync_date ){
                    last_synced.text = BaseHelper.parseDate(sync_date, com.watch.aware.app.helper.Constants.TIMEFORMAT)
                } else {
                    last_synced.text =
                        BaseHelper.parseDate(lastSync, com.watch.aware.app.helper.Constants.TIMEFORMAT)
                }
            } else {

                last_synced.text = BaseHelper.parseDate(sync_date, com.watch.aware.app.helper.Constants.TIMEFORMAT)
            }

            setAnylasisData()
        } catch (e:Exception){
            val today_date = BaseHelper.parseDate(Date(),Constants.TIME_JSON_HM)
            val sync_date = BaseHelper.parseDate(today_date,Constants.TIME_JSON_HM)
            last_synced.text = BaseHelper.parseDate(sync_date, com.watch.aware.app.helper.Constants.TIMEFORMAT)
        }
        renderData()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        welcome.text ="Welcome back, " + UserInfoManager.getInstance(activity!!).getAccountName()
        try {
            if (UserInfoManager.getInstance(activity!!)
                    .getTimeFormat() == com.watch.aware.app.helper.Constants.TWELVE_HOUR_FORMAT
            ) {
                xAxisValues = ArrayList(
                    Arrays.asList(
                        "12am",
                        "1am",
                        "2am",
                        "3am",
                        "4am",
                        "5am",
                        "6am",
                        "7am",
                        "8am",
                        "9am",
                        "10am",
                        "11am",
                        "12pm",
                        "1pm",
                        "2pm",
                        "3pm",
                        "4pm",
                        "5pm",
                        "6pm",
                        "7pm",
                        "8pm",
                        "9pm",
                        "10pm",
                        "11pm"
                    )
                )
            } else {
                xAxisValues = ArrayList(
                    Arrays.asList(
                        "12",
                        "1",
                        "2",
                        "3",
                        "4",
                        "5",
                        "6",
                        "7",
                        "8",
                        "9",
                        "10",
                        "11",
                        "12",
                        "13",
                        "14",
                        "15",
                        "16",
                        "17",
                        "18",
                        "19",
                        "20",
                        "21",
                        "22",
                        "23"
                    )
                )
            }

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
            mySQLOperate = UTESQLOperate.getInstance(activity) //
            val lmist = mySQLOperate!!.queryRunWalkAllDay()
            for(activity in lmist) {
                activity?.stepOneHourArrayInfo?.let {
                    insertStepData(activity.stepOneHourArrayInfo,activity.calendar,activity.step.toString(),String.format("%.2f", activity.calories),String.format("%.2f", activity.distance))
                }
            }
            setData()
        }catch (e:java.lang.Exception){
            e.toString()
        }

    }


    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            setData()
        }
    }

    fun setAnylasisData() {
        val dataBaseHelper = DataBaseHelper(activity)
        if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
            tv_avg_steps.text = "Average steps per hour : "
            tv_dist_travelled.text = "Max step in 1 hour :"
        } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
            tv_avg_steps.text = "Average calories burnt\nper hour : "
            tv_dist_travelled.text = "Max calories burnt\nin 1 hour :"
        } else {
            tv_avg_steps.text = "Average distance travelled\nper hour : "
            tv_dist_travelled.text = "Max distance travelled\nin 1 hour :"
        }
        try {

            val dteps = dataBaseHelper.getAllSteps(
                "WHERE date is  ('" + BaseHelper.parseDate(Date(), Constants.DATE_JSON) + "') ORDER by time DESC" )
            if (dteps != null && dteps.size > 0) {
                val lasthr = Helper.convertStringToDate(TIME_JSON_HM, dteps.get(0).time)
                val today_date = BaseHelper.parseDate(Date(),Constants.TIME_JSON_HM)
                val sync_date = BaseHelper.parseDate(today_date,Constants.TIME_JSON_HM)
                val lastSync = BaseHelper.parseDate(dteps.get(0).time, Constants.TIME_JSON_HM)
                if(lastSync > sync_date ) {
                    last_active_hr.text = BaseHelper.parseDate(sync_date, com.watch.aware.app.helper.Constants.TIMEFORMAT)
                } else{
                    last_active_hr.text = BaseHelper.parseDate(lastSync, com.watch.aware.app.helper.Constants.TIMEFORMAT)
                }
                if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
                    var avgsteps = 0
                    for(a in dteps) {
                        avgsteps = avgsteps.toInt() + a.stepCount.toInt()
                    }
                    val avg_steps = (avgsteps.toInt() / (BaseHelper.parseDate(Date(), Constants.TIME_hA).toInt()))
                    average_steps.text = avg_steps.toString()
                    max_step.text = dataBaseHelper.getMaxSteps(BaseHelper.parseDate(Date(), Constants.DATE_JSON),"stepsCount").toString()
                } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
                    var avgsteps = 0.0
                    for(a in dteps) {
                        avgsteps = avgsteps.toDouble() + a.cal.toDouble()
                    }
                    val avg_steps = (avgsteps.toFloat() / (BaseHelper.parseDate(Date(), Constants.TIME_hA).toFloat()))
                    average_steps.text = avg_steps.toInt().toString()
                    max_step.text = dataBaseHelper.getMaxSteps(BaseHelper.parseDate(Date(), Constants.DATE_JSON),"cal").toString()
                } else {
                    var avgsteps = 0.0
                    for(a in dteps) {
                        avgsteps = avgsteps.toDouble() + a.distance.toDouble()
                    }
                    val avg_steps = (avgsteps.toFloat() / (BaseHelper.parseDate(Date(), Constants.TIME_hA).toFloat()))
                    average_steps.text =String.format("%.2f",avg_steps)
                    max_step.text = dataBaseHelper.getMaxSteps(BaseHelper.parseDate(Date(), Constants.DATE_JSON),"distance").toString()
                }
            }
        } catch (e:Exception) {
            e.toString()
            average_steps.text = "0"
            max_step.text = "0"
        }
    }

    fun renderData() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }
        setAnylasisData()

        mChart.setTouchEnabled(true)
        mChart.isDragEnabled = false
        mChart.setPinchZoom(false)
        val mv = MyMarkerView(activity, R.layout.custom_marker_view)
        if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
            mv.type = "Steps"
        } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
            mv.type = "Calories"
        } else {
            mv.type = "Distance"
        }
        mv.setChartView(mChart)
        mChart.setOnChartValueSelectedListener(this)
        mChart.marker = mv

        val xAxis: XAxis = mChart.getXAxis()
        xAxis.textColor = Color.BLACK
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setDrawLimitLinesBehindData(false)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);



        val leftAxis: YAxis = mChart.getAxisLeft()
        leftAxis.removeAllLimitLines()
        if(UserInfoManager.getInstance(activity!!).getGoalType() == STEPS_GOAL) {
            leftAxis.axisMaximum = 6000f
            leftAxis.granularity = 1000f
        } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
            leftAxis.axisMaximum = 1500f
            leftAxis.granularity = 300f
        } else {
            leftAxis.axisMaximum = 5f

            leftAxis.granularity = 1f
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
            values = DailyData().getXAxisDailyGoal(activity!!,"steps")
        } else if(UserInfoManager.getInstance(activity!!).getGoalType() == CAL_GOAL) {
            values = DailyData().getXAxisDailyGoal(activity!!,"cal")
        } else {
            values = DailyData().getXAxisDailyGoal(activity!!,"dist")
        }

        val set1: LineDataSet
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = mChart.getData().getDataSetByIndex(0) as LineDataSet
            set1.values = values
            mChart.getData().notifyDataChanged()
            mChart.notifyDataSetChanged()
        } else {
            set1 = LineDataSet(values, "")
            set1.setDrawValues(false);
            set1.setDrawCircles(true);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setDrawIcons(false)
            set1.color = activity?.resources?.getColor(R.color.colorAccent)!!
            set1.setCircleColor(Color.DKGRAY)
            set1.lineWidth = 2f
            set1.circleRadius = 2f
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

    override fun onNothingSelected() {
        val x:Float = 0f
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        val x:Float =e!!.x
        val y:Float =e!!.y
        mChart.highlightValue(h)
    }
}