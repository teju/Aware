package com.watch.aware.app.helper

import android.content.Context
import com.github.mikephil.charting.data.Entry
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import java.util.*

class GoalData {
    var activity :Context? = null
    fun getXAxisStepGoal(activity: Context): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.clear()
        values.add(Entry(1f, getStepCount(0.00, 3.0)))
        values.add(Entry(2f, getStepCount(3.0, 6.0)))
        values.add(Entry(3f, getStepCount(6.0, 9.0)))
        values.add(Entry(4f, getStepCount(9.0, 12.0)))
        values.add(Entry(5f, getStepCount(12.0, 15.0)))
        values.add(Entry(6f, getStepCount(15.0, 18.0)))
        values.add(Entry(7f, getStepCount(18.0, 21.0)))
        values.add(Entry(8f, getStepCount(21.0, 23.59)))
        return values
    }
    fun getXAxisDistGoal(activity: Context): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.clear()
        values.add(Entry(1.0f, getDistanceCount(0.0, 3.0)))
        values.add(Entry(2.0f, getDistanceCount(3.0, 6.0)))
        values.add(Entry(3.0f, getDistanceCount(6.0, 9.0)))
        values.add(Entry(4.0f, getDistanceCount(9.0, 12.0)))
        values.add(Entry(5.0f, getDistanceCount(12.0, 15.0)))
        values.add(Entry(6.0f, getDistanceCount(15.0, 18.0)))
        values.add(Entry(7.0f, getDistanceCount(18.0, 21.0)))
        values.add(Entry(8.0f, getDistanceCount(21.0, 23.59)))
        return values
    }
    fun getXAxisCAlGoal(activity: Context): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.clear()
        values.add(Entry(1f, getCalCount(0.0, 3.0)))
        values.add(Entry(2f, getCalCount(3.0, 6.0)))
        values.add(Entry(3f, getCalCount(6.0, 9.0)))
        values.add(Entry(4f, getCalCount(9.0, 12.0)))
        values.add(Entry(5f, getCalCount(12.0, 15.0)))
        values.add(Entry(6f, getCalCount(15.0, 18.0)))
        values.add(Entry(7f, getCalCount(18.0, 21.0)))
        values.add(Entry(8f, getCalCount(21.0, 23.59)))
        return values
    }
    fun getDistanceCount(fromnumber : Double,toNumber : Double) :Float {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var distCnt = 0.0
        try {
            val dteps = dataBaseHelper.getAllSteps("WHERE  date is DATE('"+ BaseHelper.parseDate(
                Date(), Constants.DATE_JSON)+"') AND time >= CAST ('"+fromnumber+"' as decimal) AND  time < CAST ('"+toNumber+"' " +
                    "as decimal) ORDER BY time DESC" )
            if (dteps.size > 0) {
                for (step in dteps){
                    distCnt = distCnt + step.distance.toInt()
                    System.out.println(" getCalCount "+distCnt)
                }
            }
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return  distCnt.toFloat()
    }

    fun getCalCount(fromnumber : Double,toNumber : Double) :Float {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var calCnt = 0.0
        try {
            val dteps = dataBaseHelper.getAllSteps("WHERE  date is DATE('"+ BaseHelper.parseDate(
                Date(), Constants.DATE_JSON)+"') AND time >= CAST ('"+fromnumber+"' as decimal) AND  time < CAST ('"+toNumber+"' " +
                    "as decimal) ORDER BY time DESC" )
            if (dteps.size > 0) {
                for (step in dteps){
                    calCnt = calCnt + step.cal.toInt()
                    System.out.println(" getCalCount "+calCnt)
                }
            }
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return  calCnt.toFloat()
    }

    fun getStepCount(fromnumber : Double,toNumber : Double) :Float {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var stepsCnt = 0.0
        try {
            val dteps = dataBaseHelper.getAllSteps("WHERE  date is DATE('"+ BaseHelper.parseDate(
                Date(), Constants.DATE_JSON)+"') AND time >= CAST ('"+fromnumber+"' as decimal) AND  time < CAST ('"+toNumber+"' " +
                    "as decimal) ORDER BY time DESC" )
            if (dteps.size > 0) {
                for (step in dteps){
                    stepsCnt = stepsCnt + step.stepCount.toInt()
                    System.out.println(" getStepCount "+stepsCnt)
                }
            }
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return  stepsCnt.toFloat()
    }

}