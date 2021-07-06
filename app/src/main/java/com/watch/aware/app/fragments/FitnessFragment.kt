package com.watch.aware.app.fragments

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bestmafen.baseble.scanner.BleDevice
import com.bestmafen.baseble.scanner.BleScanCallback
import com.bestmafen.baseble.scanner.ScannerFactory
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag
import com.szabh.smable3.component.BleCache
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.BleHandleCallback
import com.szabh.smable3.entity.BleActivity
import com.szabh.smable3.entity.BleDeviceInfo
import com.szabh.smable3.entity.BleSleep
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.fragment_fitness.*
import java.util.*


class FitnessFragment : BaseFragment() {


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

            }

            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                try {
                    last_synced.text =  BaseHelper.parseDate(Date(), Constants.TIME_hMA)
                    syncing_fitness.visibility = View.GONE
                    calories.text = (activities.get(0).mCalorie/10000).toString()
                    dist.text = (activities.get(0).mDistance/1000000).toString()
                    steps.text = (activities.get(0).mStep).toString()
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
        syncing_fitness.visibility = View.VISIBLE
        swiperefresh_items.setOnRefreshListener(OnRefreshListener {

        })
        welcome.text = "Welcome back, "+ UserInfoManager.getInstance(activity!!).getAccountName()
        if(UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
            //fitness_human.setImageDrawable(activity?.resources.getDrawable(R.drawable.human_male))
        } else {
            fitness_human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_male))

        }

    }


    fun onConnected() {
        try {
            if(swiperefresh_items.isRefreshing) {
                swiperefresh_items.setRefreshing(false);
            }
        } catch (e:Exception){

        }
    }

}