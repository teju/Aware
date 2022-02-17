package com.watch.aware.app.models

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.github.mikephil.charting.data.Entry
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.watch.aware.app.helper.DataBaseHelper
import java.util.*
import kotlin.collections.ArrayList

class WeeklyData {
    var activity :Context? = null
    fun getXAxisStepWeekly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getWeeklyStepsData(0, type)))
        values.add(Entry(2.0f, getWeeklyStepsData(1, type)))
        values.add(Entry(3.0f, getWeeklyStepsData(2, type)))
        values.add(Entry(4.0f, getWeeklyStepsData(3, type)))
        values.add(Entry(5.0f, getWeeklyStepsData(4, type)))
        values.add(Entry(6.0f, getWeeklyStepsData(5, type)))
        values.add(Entry(7.0f, getWeeklyStepsData(6, type)))
        return values
    }
    fun getTotalStepWeekly(activity: Context,type : String): ArrayList<Float> {
        this.activity = activity
        val values: ArrayList<Float> = ArrayList()
        values.add(getWeeklyStepsData(0, type))
        values.add(getWeeklyStepsData(1, type))
        values.add(getWeeklyStepsData(2, type))
        values.add( getWeeklyStepsData(3, type))
        values.add( getWeeklyStepsData(4, type))
        values.add(getWeeklyStepsData(5, type))
        values.add(getWeeklyStepsData(6, type))
        return values
    }

    fun getXAxisHeartRateWeekly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getWeeklyHeartData(0, type,activity)))
        values.add(Entry(2.0f, getWeeklyHeartData(1, type, activity)))
        values.add(Entry(3.0f, getWeeklyHeartData(2, type, activity)))
        values.add(Entry(4.0f, getWeeklyHeartData(3, type, activity)))
        values.add(Entry(5.0f, getWeeklyHeartData(4, type, activity)))
        values.add(Entry(6.0f, getWeeklyHeartData(5, type, activity)))
        values.add(Entry(7.0f, getWeeklyHeartData(6, type, activity)))
        return values
    }
    fun getXAxisSpoeWeekly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getWeeklySpoData(0, type)))
        values.add(Entry(2.0f, getWeeklySpoData(1, type)))
        values.add(Entry(3.0f, getWeeklySpoData(2, type)))
        values.add(Entry(4.0f, getWeeklySpoData(3, type)))
        values.add(Entry(5.0f, getWeeklySpoData(4, type)))
        values.add(Entry(6.0f, getWeeklySpoData(5, type)))
        values.add(Entry(7.0f, getWeeklySpoData(6, type)))
        return values
    }
    fun getXAxisTempWeekly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getWeeklyTempData(0, type, activity!!)))
        values.add(Entry(2.0f, getWeeklyTempData(1, type, activity!!)))
        values.add(Entry(3.0f, getWeeklyTempData(2, type, activity!!)))
        values.add(Entry(4.0f, getWeeklyTempData(3, type, activity!!)))
        values.add(Entry(5.0f, getWeeklyTempData(4, type, activity!!)))
        values.add(Entry(6.0f, getWeeklyTempData(5, type, activity!!)))
        values.add(Entry(7.0f, getWeeklyTempData(6, type, activity!!)))
        return values
    }

    fun getWeeklyHeartData(day: Int, type: String, activity: Context) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity)
        var stepsCnt = 0
        val dteps = dataBaseHelper.getAllHeartRatesWeekly(day,BaseHelper.parseDate(Date(),Constants.DATE_MM).toInt())
        if(dteps!= null && dteps.size > 0) {
            for(heartrate in dteps) {
                stepsCnt = stepsCnt + heartrate.heartRate
            }
            stepsCnt = stepsCnt/dteps.size
        }
        return  stepsCnt.toFloat()
    }

    fun getWeeklySpoData(day : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0
        val dteps = dataBaseHelper.getAllSpoWeekly(day,BaseHelper.parseDate(Date(),Constants.DATE_MM).toInt())
        if(dteps!= null && dteps.size > 0) {
            for(heartrate in dteps) {
                stepsCnt = stepsCnt + heartrate.spoRate
            }
            stepsCnt = stepsCnt/dteps.size
        }
        return  stepsCnt.toFloat()
    }

    fun getWeeklyTempData(day: Int, type: String, activity: Context) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity)
        var stepsCnt = 0.0
        val dteps = dataBaseHelper.getAllTempWeekly(day,BaseHelper.parseDate(Date(),Constants.DATE_MM).toInt())
        if(dteps!= null && dteps.size > 0) {
            for(heartrate in dteps) {
                stepsCnt = stepsCnt + heartrate.tempRate
            }
            stepsCnt = stepsCnt/dteps.size
        }
        return  stepsCnt.toFloat()
    }


    fun getWeeklyStepsData(day : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0f
        val dteps = dataBaseHelper.getAllStepsWeekly(day,BaseHelper.parseDate(Date(),Constants.DATE_MM).toInt())
        if(dteps!= null && dteps.size > 0) {
            when (type) {
                "dist" -> {
                    for(steps in dteps){
                        stepsCnt = stepsCnt + steps.distance.toFloat()
                    }
                }
                "Steps" -> {
                    for(steps in dteps){
                        stepsCnt = stepsCnt + steps.stepCount.toInt()
                    }
                }
                "cal" -> {
                    for(steps in dteps){
                        stepsCnt = stepsCnt + steps.cal.toFloat()
                    }
                }
            }
        }
        return  stepsCnt.toFloat()
    }

}