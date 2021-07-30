package com.watch.aware.app.models

import android.content.Context
import com.github.mikephil.charting.data.Entry
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.watch.aware.app.helper.DataBaseHelper
import java.util.*

class MonthlyData {
    var activity :Context? = null
    fun getXAxisStepMonthly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getMonthlyStepsData(1, type)))
        values.add(Entry(2.0f, getMonthlyStepsData(2, type)))
        values.add(Entry(3.0f, getMonthlyStepsData(3, type)))
        values.add(Entry(4.0f, getMonthlyStepsData(4, type)))
        values.add(Entry(5.0f, getMonthlyStepsData(5, type)))
        values.add(Entry(6.0f, getMonthlyStepsData(6, type)))
        values.add(Entry(7.0f, getMonthlyStepsData(7, type)))
        values.add(Entry(8.0f, getMonthlyStepsData(8, type)))
        values.add(Entry(9.0f, getMonthlyStepsData(9, type)))
        values.add(Entry(10.0f, getMonthlyStepsData(10, type)))
        values.add(Entry(11.0f, getMonthlyStepsData(11, type)))
        values.add(Entry(12.0f, getMonthlyStepsData(12, type)))
        return values
    }
    fun getXAxisHeartonthly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getMonthlyHeartData(1, type)))
        values.add(Entry(2.0f, getMonthlyHeartData(2, type)))
        values.add(Entry(3.0f, getMonthlyHeartData(3, type)))
        values.add(Entry(4.0f, getMonthlyHeartData(4, type)))
        values.add(Entry(5.0f, getMonthlyHeartData(5, type)))
        values.add(Entry(6.0f, getMonthlyHeartData(6, type)))
        values.add(Entry(7.0f, getMonthlyHeartData(7, type)))
        values.add(Entry(8.0f, getMonthlyHeartData(8, type)))
        values.add(Entry(9.0f, getMonthlyHeartData(9, type)))
        values.add(Entry(10.0f, getMonthlyHeartData(10, type)))
        values.add(Entry(11.0f, getMonthlyHeartData(11, type)))
        values.add(Entry(12.0f, getMonthlyHeartData(12, type)))
        return values
    }
    fun getXAxisSpoMonthly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getMonthlySpoData(1, type)))
        values.add(Entry(2.0f, getMonthlySpoData(2, type)))
        values.add(Entry(3.0f, getMonthlySpoData(3, type)))
        values.add(Entry(4.0f, getMonthlySpoData(4, type)))
        values.add(Entry(5.0f, getMonthlySpoData(5, type)))
        values.add(Entry(6.0f, getMonthlySpoData(6, type)))
        values.add(Entry(7.0f, getMonthlySpoData(7, type)))
        values.add(Entry(8.0f, getMonthlySpoData(8, type)))
        values.add(Entry(9.0f, getMonthlySpoData(9, type)))
        values.add(Entry(10.0f, getMonthlySpoData(10, type)))
        values.add(Entry(11.0f, getMonthlySpoData(11, type)))
        values.add(Entry(12.0f, getMonthlySpoData(12, type)))
        return values
    }
    fun getXAxisTempMonthly(activity: Context,type : String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.add(Entry(1.0f, getMonthlyTempData(1, type)))
        values.add(Entry(2.0f, getMonthlyTempData(2, type)))
        values.add(Entry(3.0f, getMonthlyTempData(3, type)))
        values.add(Entry(4.0f, getMonthlyTempData(4, type)))
        values.add(Entry(5.0f, getMonthlyTempData(5, type)))
        values.add(Entry(6.0f, getMonthlyTempData(6, type)))
        values.add(Entry(7.0f, getMonthlyTempData(7, type)))
        values.add(Entry(8.0f, getMonthlyTempData(8, type)))
        values.add(Entry(9.0f, getMonthlyTempData(9, type)))
        values.add(Entry(10.0f, getMonthlyTempData(10, type)))
        values.add(Entry(11.0f, getMonthlyTempData(11, type)))
        values.add(Entry(12.0f, getMonthlyTempData(12, type)))
        return values
    }
    fun getMonthlyTempData(month : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0.0
        val dteps = dataBaseHelper.getAllTempMonthly(month)
        if(dteps!= null && dteps.size > 0) {
            for(steps in dteps) {
                stepsCnt = stepsCnt + steps.tempRate
            }
            stepsCnt = stepsCnt / dteps.size
        }
        return  stepsCnt.toFloat()
    }

    fun getMonthlySpoData(month : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0f
        val dteps = dataBaseHelper.getAllSpoMonthly(month)
        if(dteps!= null && dteps.size > 0) {
            for(steps in dteps) {
                stepsCnt = stepsCnt + steps.spoRate
            }
            stepsCnt = stepsCnt / dteps.size
        }
        return  stepsCnt.toFloat()
    }

    fun getMonthlyHeartData(month : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0f
        val dteps = dataBaseHelper.getAllHeartMonthly(month)
        if(dteps!= null && dteps.size > 0) {
            for(steps in dteps) {
                stepsCnt = stepsCnt + steps.heartRate
            }
            stepsCnt = stepsCnt / dteps.size
        }
        return  stepsCnt.toFloat()
    }


    fun getMonthlyStepsData(month : Int, type:String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0f
        val dteps = dataBaseHelper.getAllStepsMonthly(month)
        if(dteps!= null && dteps.size > 0) {

            when (type) {
                "dist" -> {
                    for(steps in dteps) {
                        stepsCnt = stepsCnt + steps.distance.toFloat()
                    }
                }
                "Steps" -> {
                   for(steps in dteps) {
                       stepsCnt = stepsCnt + steps.stepCount.toInt()
                   }
                }
                "cal" -> {
                    for(steps in dteps) {
                        stepsCnt = stepsCnt + steps.cal.toInt()
                    }
                }
            }
        }
        return  stepsCnt.toFloat()
    }

}