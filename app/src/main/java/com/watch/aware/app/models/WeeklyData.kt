package com.watch.aware.app.models

import android.content.Context
import com.github.mikephil.charting.data.Entry
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.watch.aware.app.helper.DataBaseHelper
import java.util.*

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
    fun getXAxisHeartRateWeekly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getWeeklyHeartData(0, type)))
        values.add(Entry(2.0f, getWeeklyHeartData(1, type)))
        values.add(Entry(3.0f, getWeeklyHeartData(2, type)))
        values.add(Entry(4.0f, getWeeklyHeartData(3, type)))
        values.add(Entry(5.0f, getWeeklyHeartData(4, type)))
        values.add(Entry(6.0f, getWeeklyHeartData(5, type)))
        values.add(Entry(7.0f, getWeeklyHeartData(6, type)))
        return values
    }
    fun getWeeklyHeartData(day : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
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



    fun getWeeklyStepsData(day : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0
        val dteps = dataBaseHelper.getAllStepsWeekly(day,BaseHelper.parseDate(Date(),Constants.DATE_MM).toInt())
        if(dteps!= null && dteps.size > 0) {

            when (type) {
                "dist" -> {
                    return dteps.get(0).total_dist.toFloat()
                }
                "Steps" -> {
                    stepsCnt = stepsCnt + dteps.get(0).total_count.toInt()
                }
                "cal" -> {
                    stepsCnt = stepsCnt + dteps.get(0).total_cal.toInt()
                }
            }
        }
        return  stepsCnt.toFloat()
    }

}