package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bestmafen.baseble.scanner.BleDevice
import com.bestmafen.baseble.scanner.BleScanCallback
import com.bestmafen.baseble.scanner.ScannerFactory
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.BleActivity
import com.szabh.smable3.entity.BleDeviceInfo
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Helper
import kotlinx.android.synthetic.main.fragment_insight.*
import kotlinx.android.synthetic.main.fragment_insight.swiperefresh_items
import kotlinx.android.synthetic.main.fragment_welness.*


class InsightsFragment : BaseFragment() {

    fun setBottomNavigation(bottomNavigation: BottomNavigationView?) {
        this.bottomNavigation = bottomNavigation
    }

    private var barDataSet: BarDataSet? = null
    private var bottomNavigation: BottomNavigationView? = null
    val barEntries = ArrayList<BarEntry>()
    private val mBleHandleCallback by lazy {
        object : BleHandleCallback {

            override fun onDeviceConnected(_device: BluetoothDevice) {
                onConnected(R.color.colorPrimary)
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
                onConnected(R.color.colorPrimary)
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
        checkBoxFun()
        connect()
    }
    fun connect() {
        if(BleCache.mDeviceInfo != null) {
            onConnected(R.color.colorPrimary)
            Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)

        }
    }

    fun onConnected(colour : Int) {
        if(swiperefresh_items.isRefreshing) {
            swiperefresh_items.setRefreshing(false);
        }
        getEntries()
        barDataSet = BarDataSet(barEntries, "")
        val barData = BarData(barDataSet)
        idBarChart.setData(barData)
        idBarChart.getAxisRight().setEnabled(false);
        idBarChart.getAxisLeft().setTextColor(ContextCompat.getColor(activity!!, colour)); // left y-axis

        barDataSet?.setColors(context?.resources?.getColor(R.color.colorPrimaryDark)!!)
        barDataSet?.setValueTextColor(Color.WHITE)
        barDataSet?.setValueTextSize(18f)


    }
    private fun getEntries() {

        barEntries.add(BarEntry(2f, 0f))
        barEntries.add(BarEntry(4f, 1000f))
        barEntries.add(BarEntry(6f, 1000f))
        barEntries.add(BarEntry(8f, 3000f))
        barEntries.add(BarEntry(7f, 4000f))
        barEntries.add(BarEntry(3f, 3000f))
    }

    fun checkBoxFun() {

        distnce.setOnClickListener {
            steps.isChecked = false
            calories.isChecked = false
            barDataSet?.setColors(context?.resources?.getColor(R.color.colorPrimaryDark)!!)


        }
        steps.setOnClickListener {
            distnce.isChecked = false
            calories.isChecked = false
            onConnected(R.color.Blue)

        }
        calories.setOnClickListener {
            steps.isChecked = false
            distnce.isChecked = false
            onConnected(R.color.orange)

        }
        idBarChart.notifyDataSetChanged();
        idBarChart.invalidate();
    }
}