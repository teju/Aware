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
import kotlinx.android.synthetic.main.fragment_welness.*
import java.lang.Exception


class WelnessFragment : BaseFragment() {

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
                    device.setText(R.string.enable_bluetooth)
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
                try {
                    device.text = "Manufaturer : "+_device.name
                } catch (e:Exception){

                }

                onConnected()
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
               onConnected()
            }


            override fun onReadHeartRate(heartRates: List<BleHeartRate>) {
                try {
                    heart_rate.text = heartRates.get(0).mBpm.toString()+" BPM"

                }catch (e:Exception) {

                }
            }

            override fun onReadTemperature(temperatures: List<BleTemperature>) {
                try {
                    temp.text = temperatures.get(0).mTemperature.toString()+" C"

                }catch (e:Exception) {

                }
            }

            override fun onReadBloodOxygen(bloodOxygen: List<BleBloodOxygen>) {
                try {
                    oxygen.text = bloodOxygen.get(0).mValue.toString()+" %"

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
        v =  inflater.inflate(R.layout.fragment_welness, container, false)
        return v;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        syncing.visibility = View.VISIBLE
        BleConnector.addHandleCallback(mBleHandleCallback)
        mBleScanner.scan(!mBleScanner.isScanning)
        connect()
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

        if(BleCache.mDeviceInfo?.mBleName != null){
            device.text = "Manufaturer : "+BleCache.mDeviceInfo?.mBleName

        }
        connection_status.text = "Connection Status: : Connected"
        Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
        bluetooth.setColorFilter(ContextCompat.getColor(activity!!, R.color.Blue), android.graphics.PorterDuff.Mode.SRC_IN);
        syncing.visibility = View.GONE
        } catch (e:Exception){

        }

    }

}