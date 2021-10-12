package com.watch.aware.app.fragments.graphs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
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
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.models.DailyData
import com.watch.aware.app.helper.MyMarkerView
import com.watch.aware.app.helper.UserInfoManager
import com.watch.aware.app.models.MonthlyData
import com.watch.aware.app.models.WeeklyData
import kotlinx.android.synthetic.main.fragment_step_graph.*
import java.util.*


class StepsGraphFragment : BaseFragment(),View.OnClickListener, OnChartValueSelectedListener {
    companion object {
        val DAILY = 1
        val WEEKLY = 2
        val MONTHLY = 3
    }
    var xAxisValues: List<String> = ArrayList()
    var values: MutableList<Entry> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_step_graph, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llback.setOnClickListener(this)
        week.setOnClickListener(this)
        day.setOnClickListener(this)
        month.setOnClickListener(this)
        val todayDate = BaseHelper.parseDate(Date(),Constants.DATE_MONTH)
        today_date.text = todayDate
        val stepsArray = lastestHRSteps()
        if(stepsArray!= null && stepsArray?.size != 0) {
            stepsCount.text = stepsArray.get(0).total_steps.toString()
        }
        setXaxisData(DAILY)
    }

    fun setXaxisData(which : Int) {
        xAxisValues = ArrayList()
        if(which == DAILY) {
            if(UserInfoManager.getInstance(activity!!).getTimeFormat() == com.watch.aware.app.helper.Constants.TWELVE_HOUR_FORMAT) {
                xAxisValues = ArrayList(
                    Arrays.asList("12am",
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
                        "9pm" ,
                        "10pm" ,
                        "11pm" ))
            } else {
                xAxisValues = ArrayList(
                    Arrays.asList("12",
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
                        "23"))
            }

        } else if(which == WEEKLY){
            xAxisValues = ArrayList(
                Arrays.asList("",
                    "Sun",
                    "Mon",
                    "Tue",
                    "Wed",
                    "Thur",
                    "Fri",
                    "Sat"))
        }
        else if(which == MONTHLY){
            xAxisValues = ArrayList(
                Arrays.asList("",
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"))
        }
        values.clear()
        if(which == DAILY) {
            values = DailyData().getXAxisDailyGoal(activity!!,"steps")
        } else if(which == WEEKLY) {
            values = WeeklyData().getXAxisStepWeekly(activity!!,"Steps")
        } else if(which == MONTHLY) {
            values = MonthlyData().getXAxisStepMonthly(activity!!,"Steps")
        }
        renderData(which)
        mChart.notifyDataSetChanged()
        mChart.invalidate()
    }

    fun renderData(which : Int) {
        try {

            mChart.setTouchEnabled(true)
            mChart.isDragEnabled = false
            mChart.setPinchZoom(false)
            val mv = MyMarkerView(activity, R.layout.custom_marker_view)
            mv.type = "Steps"
            mv.setChartView(mChart)
            mChart.setOnChartValueSelectedListener(this)
            mChart.marker = mv

            val xAxis: XAxis = mChart.getXAxis()
            xAxis.textColor = Color.BLACK
            xAxis.setDrawLimitLinesBehindData(false)
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAvoidFirstLastClipping(true);

            val leftAxis: YAxis = mChart.getAxisLeft()
            leftAxis.removeAllLimitLines()
            if(which == DAILY) {
                leftAxis.axisMaximum = 6000f
                leftAxis.granularity = 1000f
            } else if(which == WEEKLY) {
                leftAxis.axisMaximum = 15000f
                leftAxis.granularity = 3000f
            } else if(which == MONTHLY) {
                leftAxis.axisMaximum = 300000f
                leftAxis.granularity = 50000f
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


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.llback -> {
                home()?.proceedDoOnBackPressed()
            }
            R.id.week -> {
                week.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                day.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
                month.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
                ViewCompat.setBackgroundTintList(
                    week,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.black)!!));
                ViewCompat.setBackgroundTintList(
                    day,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.light_gray)!!));
                ViewCompat.setBackgroundTintList(
                    month,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.light_gray)!!));
                setXaxisData(WEEKLY)
            }
            R.id.day -> {
                week.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
                day.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                month.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
                ViewCompat.setBackgroundTintList(
                    day,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.black)!!));
                ViewCompat.setBackgroundTintList(
                    week,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.light_gray)!!));
                ViewCompat.setBackgroundTintList(
                    month,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.light_gray)!!));
                setXaxisData(DAILY)
            }
            R.id.month -> {
                week.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
                day.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
                month.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                ViewCompat.setBackgroundTintList(
                    month,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.black)!!));
                ViewCompat.setBackgroundTintList(
                    day,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.light_gray)!!));
                ViewCompat.setBackgroundTintList(
                    week,
                    ColorStateList.valueOf(activity?.resources?.getColor(R.color.light_gray)!!));
                setXaxisData(MONTHLY)
            }
        }
    }

    override fun onNothingSelected() {}

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        val x:Float =e!!.x
        val y:Float =e!!.y
        mChart.highlightValue(h)
    }

}