package com.watch.aware.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.TIMEFORMAT
import com.watch.aware.app.helper.UserInfoManager
import com.yc.pedometer.sdk.*
import com.yc.pedometer.update.Updates
import com.yc.pedometer.utils.GlobalVariable
import com.yc.pedometer.utils.LogUtils
import com.yc.pedometer.utils.SPUtil
import kotlinx.android.synthetic.main.fragment_fitness.*
import java.util.*


class FitnessFragment : BaseFragment() , ServiceStatusCallback {
    private var mSteps = 0
    private var mDistance = 0f
    private var mCalories = 0f
    private var mRunCalories: kotlin.Float = 0f
    private var mWalkCalories: kotlin.Float = 0f
    private var mRunSteps =
        0
    private var mRunDurationTime: Int = 0
    private var mWalkSteps: Int = 0
    private var mWalkDurationTime: Int = 0
    private var mRunDistance = 0f
    private var mWalkDistance: kotlin.Float = 0f
    protected val TAG = "FitnessFragment"

/*
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



            override fun onReadBloodOxygen(bloodOxygen: List<BleBloodOxygen>) {
                super.onReadBloodOxygen(bloodOxygen)
                try {
                    SpoRateInsert(bloodOxygen)
                } catch (e:java.lang.Exception){

                }
            }



            override fun onReadActivity(activities: List<BleActivity>) {
                super.onReadActivity(activities)
                try {
                    syncing_fitness.visibility = View.GONE
                    calories.text = (activities.get(0).mCalorie/10000).toString()
                    val mDistance = (activities.get(0).mDistance/10000).toDouble()
                    val mDist = (mDistance/1000).toDouble()
                    dist.text = String.format("%.2f",mDist)
                    steps.text = (activities.get(0).mStep).toString()
                    if(swiperefresh_items.isRefreshing) {
                        swiperefresh_items.setRefreshing(false);
                    }
                }catch (e:Exception) {
                    e.printStackTrace()
                }
                onConnected()
                insertStepData(activities)

            }
        }
    }
*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_fitness, container, false)
        return v;
    }

    fun setData() {
        val stepsArray = lastestHRSteps()
        if (stepsArray != null && stepsArray?.size != 0) {
            val sync_date = BaseHelper.parseDate(stepsArray?.get(0)?.time, Constants.TIME_JSON_HM)
            last_synced.text = BaseHelper.parseDate(sync_date, TIMEFORMAT)
            calories.text = String.format("%.2f", stepsArray?.get(0)?.total_cal?.toDouble())
            dist.text = String.format("%.2f", stepsArray?.get(0)?.total_dist?.toDouble())
            steps.text = stepsArray?.get(0)?.total_count.toString()
        } else {
            val today_date = BaseHelper.parseDate(Date(), Constants.TIME_JSON_HM)
            val sync_date = BaseHelper.parseDate(today_date, Constants.TIME_JSON_HM)
            last_synced.text = BaseHelper.parseDate(sync_date, TIMEFORMAT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            syncing_fitness.visibility = View.VISIBLE
            swiperefresh_items.setOnRefreshListener(OnRefreshListener {


            })
            welcome.text =
                "Welcome back, " + UserInfoManager.getInstance(activity!!).getAccountName()
            if (UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
                fitness_human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_female))
            } else {
                fitness_human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_male))
            }
            setData()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        refresh.setOnClickListener {

        }
        mySQLOperate = UTESQLOperate.getInstance(activity) //
        mDataProcessing = DataProcessing.getInstance(mContext)
        mDataProcessing?.setOnStepChangeListener(mOnStepChangeListener)

        mWriteCommand = WriteCommandToBLE.getInstance(mContext)
        mUpdates = Updates.getInstance(mContext)

//
        val ble_connecte = SPUtil.getInstance(mContext).bleConnectStatus

        if (ble_connecte) {
            mWriteCommand?.sendStepLenAndWeightToBLE(
                170, 65, 5,
                10000, true, true, 150, true, 20, false, true, 50, GlobalVariable.TMP_UNIT_CELSIUS, true
            )


        }
        activity?.runOnUiThread {
            val list =
                mySQLOperate!!.queryRunWalkAllDay()
            if (list != null) {
                for (i in list.indices) {
                    val calendar = list[i].walkDurationTime
                    val step = list[i].step
                    val runSteps = list[i].runSteps
                    val walkSteps = list[i].walkSteps
                    LogUtils.d(
                        TAG, "queryRunWalkAllDay calendar =" + calendar
                                + ",step =" + step + ",runSteps =" + runSteps
                                + ",walkSteps =" + walkSteps
                    )
                }
                insertStepData(list)
                setData()
            }

        }


    }

    val mOnStepChangeListener =
        StepChangeListener { info ->
            if (info != null) {
                mSteps = info.step
                mDistance = info.distance
                mCalories = info.calories
                mRunSteps = info.runSteps
                mRunCalories = info.runCalories
                mRunDistance = info.runDistance
                mRunDurationTime = info.runDurationTime
                mWalkSteps = info.walkSteps
                mWalkCalories = info.walkCalories
                mWalkDistance = info.walkDistance
                mWalkDurationTime = info.walkDurationTime
            }
            LogUtils.d(
                TAG, "mSteps =" + mSteps + ",mDistance ="
                        + mDistance + ",mCalories =" + mCalories + ",mRunSteps ="
                        + mRunSteps + ",mRunCalories =" + mRunCalories
                        + ",mRunDistance =" + mRunDistance + ",mRunDurationTime ="
                        + mRunDurationTime + ",mWalkSteps =" + mWalkSteps
                        + ",mWalkCalories =" + mWalkCalories + ",mWalkDistance ="
                        + mWalkDistance + ",mWalkDurationTime ="
                        + mWalkDurationTime
            )
            activity?.runOnUiThread {
                val list =
                    mySQLOperate!!.queryRunWalkAllDay()
                if (list != null) {
                    for (i in list.indices) {
                        val calendar = list[i].calendar
                        val step = list[i].step
                        val runSteps = list[i].runSteps
                        val walkSteps = list[i].walkSteps
                        LogUtils.d(
                            TAG, "queryRunWalkAllDay calendar =" + calendar
                                    + ",step =" + step + ",runSteps =" + runSteps
                                    + ",walkSteps =" + walkSteps
                        )
                    }
                    insertStepData(list)
                    setData()
                }

            }
        }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            setData()
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

    override fun OnServiceStatuslt(p0: Int) {

    }

}