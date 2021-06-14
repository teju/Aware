package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bestmafen.baseble.scanner.BleDevice
import com.bestmafen.baseble.scanner.BleScanCallback
import com.bestmafen.baseble.scanner.ScannerFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.*
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Helper
import kotlinx.android.synthetic.main.fragment_fitness.*
import java.lang.Exception


class FitnessFragment : BaseFragment() {

    fun setBottomNavigation(bottomNavigation: BottomNavigationView?) {
        this.bottomNavigation = bottomNavigation
    }
    private var bottomNavigation: BottomNavigationView? = null
    private val mBleScanner by lazy {
        // ScannerFactory.newInstance(arrayOf(UUID.fromString(BleConnector.BLE_SERVICE)))
        ScannerFactory.newInstance()
            .setScanDuration(10)
            .setBleScanCallback(object : BleScanCallback {

                override fun onBluetoothDisabled() {

                }

                override fun onScan(scan: Boolean) {
                    try {
                        if (swiperefresh_items.isRefreshing) {
                            swiperefresh_items.setRefreshing(false);
                        }
                    }catch (e:Exception){

                    }
                }

                override fun onDeviceFound(device: BleDevice) {
                    try {
                        if (swiperefresh_items.isRefreshing) {
                            swiperefresh_items.setRefreshing(false);

                        }
                    }catch (e:Exception){

                    }
                    if(device.mBluetoothDevice.address.equals("FA:B4:2E:A8:5E:03")) {
                        BleConnector.setBleDevice(device).connect(true)
                    }
                }
            })
    }
    private val mBleHandleCallback by lazy {
        object : BleHandleCallback {

            override fun onDeviceConnected(_device: BluetoothDevice) {
                onConnected()
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
               onConnected()
            }

            override fun onReadSleep(sleeps: List<BleSleep>) {
                super.onReadSleep(sleeps)
                try {
                    sleep.text = "Slept Last Night\n Light Sleep :"+sleeps.get(0).mSoft+" hrs\nDeep Sleep :"+sleeps.get(0).mStrong+" hrs"
                }catch (e:Exception) {

                }
            }

            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                try {
                    syncing_fitness.visibility = View.GONE
                    calories.text = "Burned\n"+(activities.get(0).mCalorie/10000)+" Kacl"
                    distance.text = "Distance Travelled\n"+(activities.get(0).mDistance/10000)+" Km"
                    steps_cnt.text = "Steps\n"+(activities.get(0).mStep)
                }catch (e:Exception) {

                }

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_fitness, container, false)
        return v;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BleConnector.addHandleCallback(mBleHandleCallback)
        connect()
        syncing_fitness.visibility = View.VISIBLE

        swiperefresh_items.setOnRefreshListener(OnRefreshListener {
           connect()
        })
    }
    fun connect() {
        if(BleCache.mDeviceInfo != null) {
           onConnected()
        } else {
            mBleScanner.scan(!mBleScanner.isScanning)

        }
    }

    fun onConnected() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }
        } catch (e:Exception){

        }

        Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)

    }

}