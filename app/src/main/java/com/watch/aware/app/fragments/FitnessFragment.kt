package com.watch.aware.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.TIMEFORMAT
import com.watch.aware.app.helper.UserInfoManager
import com.yc.pedometer.info.StepOneDayAllInfo
import com.yc.pedometer.sdk.*
import com.yc.pedometer.update.Updates
import com.yc.pedometer.utils.CalendarUtils
import com.yc.pedometer.utils.LogUtils
import kotlinx.android.synthetic.main.fragment_fitness.*
import java.util.*


class FitnessFragment : BaseFragment()  {
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



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_fitness, container, false)
        return v;
    }

    fun setData(info: StepOneDayAllInfo) {
       // val stepsArray = lastestHRSteps()
        if (info != null) {
            last_synced.text =BaseHelper.parseDate(Date(), TIMEFORMAT)
            calories.text = String.format("%.2f", info.calories)
            dist.text = String.format("%.2f", info.distance)
            steps.text = info.step.toString()
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

            welcome.text =
                "Welcome back, " + UserInfoManager.getInstance(activity!!).getAccountName()
            if (UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
                fitness_human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_female))
            } else {
                fitness_human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_male))
            }
           // setData(info)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        mySQLOperate = UTESQLOperate.getInstance(activity) //
        mDataProcessing = DataProcessing.getInstance(mContext)
        mDataProcessing?.setOnStepChangeListener(mOnStepChangeListener)
        mDataProcessing?.setOnSleepChangeListener(mOnSleepChangeListener)

        mWriteCommand = WriteCommandToBLE.getInstance(mContext)
        mUpdates = Updates.getInstance(mContext)
        mWriteCommand?.syncAllStepData()
         activity?.runOnUiThread {
             val sleepTimeInfo = UTESQLOperate.getInstance(mContext)
                 .querySleepInfo(CalendarUtils.getCalendar(0))
             if (sleepTimeInfo != null) {
                 sleep.text =  ""+sleepTimeInfo.sleepTotalTime/60
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
                syncing_fitness.visibility = View.GONE

                connection_status.setText(getString(R.string.connected))
                connection_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_circle, 0);
                setData(info)
            }

        }

    private val mOnSleepChangeListener =
        SleepChangeListener {

        }




}