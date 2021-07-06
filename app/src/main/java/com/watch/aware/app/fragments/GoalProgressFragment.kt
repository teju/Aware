package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
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
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.BleActivity
import com.szabh.smable3.entity.BleDeviceInfo
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.MyMarkerView
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.fragment_goal_progress.*
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
            "9 pm")
    )
    val values: MutableList<Entry> = ArrayList()
    private val mBleHandleCallback by lazy {
        object : BleHandleCallback {

            override fun onDeviceConnected(_device: BluetoothDevice) {
                renderData()
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
               renderData()
            }


            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                try {
                    last_synced.text  = BaseHelper.parseDate(Date(), Constants.TIME_hMA)
                    val amount: Double = (activities.get(0).mStep).toDouble()
                    val res = amount / 10000 * 100
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                       // progressBar.setProgress(20,true)
                    }
                    step_count.text = ""+ String.format("%.2f",res) + "%\nof steps taken"

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
        })
        welcome.text = "Welcome back, "+ UserInfoManager.getInstance(activity!!).getAccountName()
    }

    fun connect() {
        if(BleCache.mDeviceInfo != null) {
           renderData()
        }
    }
    fun renderData() {
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


        setData()

    }

    private fun setData() {
        values.clear()
        values.add(Entry(1f, isValideData(0.0,2.9)))
        values.add(Entry(2f, isValideData(3.0,5.9) ))
        values.add(Entry(3f, isValideData(6.0,8.9)))
        values.add(Entry(4f, isValideData(9.0,11.9)))
        values.add(Entry(5f, isValideData(12.0,14.9)))
        values.add(Entry(6f, isValideData(15.0,17.9)))
        values.add(Entry(7f, isValideData(18.0,20.9)))
        values.add(Entry(8f, isValideData(21.0,23.9)))



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

    fun isValideData(fromnumber : Double,toNumber : Double) :Float {

        val dataBaseHelper = DataBaseHelper(activity!!)
        val query = dataBaseHelper.getAllSteps("ORDER BY stepsCount DESC")

        var stepsCnt = 0.0
        try {
            val lastHour =  BaseHelper.parseDate(query.get(0).time,TIME_JSON_HM)
            last_active_hr.text = BaseHelper.parseDate(lastHour,TIME_hM)
            average_steps.text = (query.get(0).stepCount.toInt()/query.get(0).time.toDouble()).toInt().toString() + " Steps/hr"

            for (i in fromnumber.toInt()..toNumber.toInt()) {
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

        return  stepsCnt.toFloat()
    }
}