package com.watch.aware.app.fragments

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.se.omapi.SEService
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.watch.aware.app.R
import com.watch.aware.app.callback.DeviceItemClickListener
import com.watch.aware.app.fragments.dialog.DeviceListingDialogFragment
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.models.BleDevices
import com.yc.pedometer.info.CustomTestStatusInfo
import com.yc.pedometer.info.HeartRateHeadsetSportModeInfo
import com.yc.pedometer.info.SportsModesInfo
import com.yc.pedometer.sdk.*
import com.yc.pedometer.utils.LogUtils
import com.yc.pedometer.utils.SPUtil
import kotlinx.android.synthetic.main.fragment_connection.*
import java.util.*


@RequiresApi(Build.VERSION_CODES.P)
class ConnectionFragment : BaseFragment(),View.OnClickListener, DeviceScanInterfacer,
    SEService.OnConnectedListener {
    private val TAG = "ConnectionFragment"

    private var mScanning = false
    private var mHandler: Handler? = null

    private val REQUEST_ENABLE_BT = 1

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private var mBLEServiceOperate: BLEServiceOperate? = null
    private var mBluetoothLeService: BluetoothLeService? = null

    var arrayList = java.util.ArrayList<BleDevices>()
    var isFromSettings = false

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

        tvtry_again.setText(Html.fromHtml("or <font color='#02FE97'>try again</font>"))
        tv_contact_support.setText(Html.fromHtml("<u>contact a’ware Support.</u>"))

        mHandler = Handler()
        mBLEServiceOperate = BLEServiceOperate
            .getInstance(activity)

        // Checks if Bluetooth is supported on the device.
        if (!mBLEServiceOperate!!.isSupportBle4_0()) {
            Toast.makeText(
                activity,
                R.string.not_support_ble,
                Toast.LENGTH_SHORT
            )
                .show()
            activity?.finish()
            return
        }
        mBLEServiceOperate!!.setDeviceScanListener(this)

        scanLeDevice(true)
    }
     override fun onResume() {
        super.onResume()
        if (!mBLEServiceOperate!!.isBleEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
     override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
            && resultCode == Activity.RESULT_CANCELED) {
            activity?.finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()

    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                mScanning = false
                mBLEServiceOperate!!.stopLeScan()
                search.text = "Scan"
                if(arrayList.size != 0) {
                    showDeviceListingDialog(
                        arrayList,
                        object : DeviceItemClickListener {
                            override fun onItemClick(pos: Int) {
                                val device: BleDevices = arrayList.get(pos) ?: return
                                SPUtil.getInstance(activity?.getApplicationContext()).lastConnectDeviceAddress =
                                    device.getAddress()
                                if(mScanning) {
                                    mBLEServiceOperate!!.stopLeScan()
                                    mScanning = false
                                }
                                mBLEServiceOperate!!.connect(device.address)
                                // 如果没在搜索界面提前实例BLEServiceOperate的话，下面这4行需要放到OnServiceStatuslt
                                mBluetoothLeService = mBLEServiceOperate!!.bleService
                                if (mBluetoothLeService != null) {
                                    if (isFromSettings) {
                                        home()?.proceedDoOnBackPressed()
                                    } else {
                                        home()?.setFragment(CoughSettingsFragment())
                                    }
                                }
                            }
                        })
                }
            }, SCAN_PERIOD)
            mScanning = true
            mBLEServiceOperate!!.startLeScan()
            search.text = "Searching ..."
            LogUtils.i(TAG, "startLeScan")
        } else {

            mScanning = false
            mBLEServiceOperate!!.stopLeScan()
        }

    }

    fun showDeviceListingDialog(
        arrayList: ArrayList<BleDevices>,
        n: DeviceItemClickListener?) {
        val f = DeviceListingDialogFragment()
        f.listener = n
        f.arrayList = arrayList
        f.isCancelable = true
        if(! f.isVisible) {
            f.show(activity!!.supportFragmentManager, DeviceListingDialogFragment.TAG)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.search -> {
                scanLeDevice(true)
            }
        }
    }

    override fun onBackTriggered() {
        if(isFromSettings) {
            home()?.proceedDoOnBackPressed()
        } else {
            home()?.exitApp()
        }

    }

    override fun LeScanCallback(device: BluetoothDevice?, rssi: Int, p2: ByteArray?) {

        activity?.runOnUiThread(Runnable {
            // mLeDeviceListAdapter.addDevice(device);
            //				LogUtils.i(TAG,"device="+device);
            if (device != null) {
                if (TextUtils.isEmpty(device.getName())) {
                    return@Runnable
                }

                val mBleDevices = BleDevices(
                    device.getName(),
                    device.getAddress(), rssi
                )
                if(!arrayList.contains(mBleDevices)) {
                    arrayList.add(mBleDevices)
                }

            }
        })

    }




    override fun onConnected() {

    }
}