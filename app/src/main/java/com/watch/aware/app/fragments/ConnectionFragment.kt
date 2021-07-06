package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bestmafen.baseble.scanner.BleDevice
import com.bestmafen.baseble.scanner.BleScanCallback
import com.bestmafen.baseble.scanner.ScannerFactory
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.*
import com.watch.aware.app.R
import com.watch.aware.app.callback.DeviceItemClickListener
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.dialog.DeviceListingDialogFragment
import com.watch.aware.app.fragments.dialog.NotifyDialogFragment
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Helper
import kotlinx.android.synthetic.main.fragment_connection.*


class ConnectionFragment : BaseFragment(),View.OnClickListener {
    var arrayList = java.util.ArrayList<BleDevice>()

    val mBleScanner by lazy {
        // ScannerFactory.newInstance(arrayOf(UUID.fromString(BleConnector.BLE_SERVICE)))
        ScannerFactory.newInstance()
            .setScanDuration(10)
            .setBleScanCallback(object : BleScanCallback {

                override fun onBluetoothDisabled() {

                }

                override fun onScan(scan: Boolean) {
                    if(scan) {
                        search.text = "Searching ..."
                    } else {
                        search.text = "Scan"
                        showDeviceListingDialog(arrayList,object :DeviceItemClickListener {
                            override fun onItemClick(pos: Int) {
                                BleConnector.setBleDevice(arrayList.get(pos)).connect(true)
                            }

                        })
                    }
                }

                override fun onDeviceFound(device: BleDevice) {
                    arrayList.add(device)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_connection, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search.setOnClickListener(this)
        mBleScanner.scan(!mBleScanner.isScanning)
        BleConnector.addHandleCallback(mBleHandleCallback)
        tvtry_again.setText(Html.fromHtml("or <font color='#02FE97'>try again</font>"))
        tv_contact_support.setText(Html.fromHtml("<u>contact a’ware Support.</u>"))

    }

    fun showDeviceListingDialog(arrayList:ArrayList<BleDevice>,n: DeviceItemClickListener?) {
        val f = DeviceListingDialogFragment()
        f.listener = n
        f.arrayList = arrayList
        f.isCancelable = false
        f.show(activity!!.supportFragmentManager, DeviceListingDialogFragment.TAG)

    }

    val mBleHandleCallback by lazy {
        object : BleHandleCallback {
            override fun onDeviceConnected(_device: BluetoothDevice) {

                    home()?.setFragment(CoughSettingsFragment())
            }

            override fun onIdentityCreate(status: Boolean, deviceInfo: BleDeviceInfo?) {
                Helper.handleCommand(BleKey.DATA_ALL, BleKeyFlag.READ,activity!!)
            }

            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                insertStepData(activities)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            mBleScanner.scan(!mBleScanner.isScanning)
        }
    }
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.search -> {
                mBleScanner.scan(!mBleScanner.isScanning)
            }
        }
    }

}