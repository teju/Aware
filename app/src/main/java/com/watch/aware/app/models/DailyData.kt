package com.watch.aware.app.models

import android.content.Context
import com.github.mikephil.charting.data.Entry
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.watch.aware.app.helper.DataBaseHelper
import java.util.*

class DailyData {
    var activity :Context? = null

    fun getXAxisDailyGoal(activity: Context,type:String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.clear()
        values.add(Entry(1f, getStepCount(0.00, 1.0,type)))
        values.add(Entry(2f, getStepCount(1.0, 2.0, type)))
        values.add(Entry(3f, getStepCount(2.0, 3.0, type)))
        values.add(Entry(4f, getStepCount(3.0, 4.0, type)))
        values.add(Entry(5f, getStepCount(4.0, 5.0, type)))
        values.add(Entry(6f, getStepCount(5.0, 6.0, type)))
        values.add(Entry(7f, getStepCount(6.0, 7.0, type)))
        values.add(Entry(8f, getStepCount(7.0, 8.0, type)))
        values.add(Entry(9f, getStepCount(8.0, 9.0, type)))
        values.add(Entry(10f, getStepCount(9.0, 10.0, type)))
        values.add(Entry(11f, getStepCount(10.0, 11.0, type)))
        values.add(Entry(12f, getStepCount(11.0, 12.0, type)))
        values.add(Entry(13f, getStepCount(12.0, 13.0, type)))
        values.add(Entry(14f, getStepCount(13.0, 14.0, type)))
        values.add(Entry(15f, getStepCount(14.0, 15.0, type)))
        values.add(Entry(16f, getStepCount(15.0, 16.0, type)))
        values.add(Entry(17f, getStepCount(16.0, 17.0, type)))
        values.add(Entry(18f, getStepCount(17.0, 18.0, type)))
        values.add(Entry(19f, getStepCount(18.0, 19.0, type)))
        values.add(Entry(20f, getStepCount(19.0, 20.0, type)))
        values.add(Entry(21f, getStepCount(20.0, 21.0, type)))
        values.add(Entry(22f, getStepCount(21.0, 22.0, type)))
        values.add(Entry(23f, getStepCount(22.0, 23.0, type)))
        values.add(Entry(24f, getStepCount(23.0, 23.59, type)))
        return values
    }

    fun getXAxisDailyHeart(activity: Context,type:String): MutableList<Entry> {
        this.activity = activity
        val values: MutableList<Entry> = ArrayList()
        values.clear()
        values.add(Entry(1f, getHeartRateCount(0.00, 1.0,type)))
        values.add(Entry(2f, getHeartRateCount(1.0, 2.0, type)))
        values.add(Entry(3f, getHeartRateCount(2.0, 3.0, type)))
        values.add(Entry(4f, getHeartRateCount(3.0, 4.0, type)))
        values.add(Entry(5f, getHeartRateCount(4.0, 5.0, type)))
        values.add(Entry(6f, getHeartRateCount(5.0, 6.0, type)))
        values.add(Entry(7f, getHeartRateCount(6.0, 7.0, type)))
        values.add(Entry(8f, getHeartRateCount(7.0, 8.0, type)))
        values.add(Entry(9f, getHeartRateCount(8.0, 9.0, type)))
        values.add(Entry(10f, getHeartRateCount(9.0, 10.0, type)))
        values.add(Entry(11f, getHeartRateCount(10.0, 11.0, type)))
        values.add(Entry(12f, getHeartRateCount(11.0, 12.0, type)))
        values.add(Entry(13f, getHeartRateCount(12.0, 13.0, type)))
        values.add(Entry(14f, getHeartRateCount(13.0, 14.0, type)))
        values.add(Entry(15f, getHeartRateCount(14.0, 15.0, type)))
        values.add(Entry(16f, getHeartRateCount(15.0, 16.0, type)))
        values.add(Entry(17f, getHeartRateCount(16.0, 17.0, type)))
        values.add(Entry(18f, getHeartRateCount(17.0, 18.0, type)))
        values.add(Entry(19f, getHeartRateCount(18.0, 19.0, type)))
        values.add(Entry(20f, getHeartRateCount(19.0, 20.0, type)))
        values.add(Entry(21f, getHeartRateCount(20.0, 21.0, type)))
        values.add(Entry(22f, getHeartRateCount(21.0, 22.0, type)))
        values.add(Entry(23f, getHeartRateCount(22.0, 23.0, type)))
        values.add(Entry(24f, getHeartRateCount(23.0, 23.59, type)))
        return values
    }


    fun getStepCount(fromnumber: Double, toNumber: Double, type: String) :Float {

        val dataBaseHelper =
            DataBaseHelper(activity!!)
        var stepsCnt = 0.0
        try {
            val dteps = dataBaseHelper.getAllSteps("WHERE  date is DATE('"+ BaseHelper.parseDate(
                Date(), Constants.DATE_JSON)+"') AND time >= CAST ('"+fromnumber+"' as decimal) AND  time < CAST ('"+toNumber+"' " +
                    "as decimal) ORDER BY time DESC" )
            if (dteps.size > 0) {
                when(type) {
                    "steps" -> {
                        for (step in dteps){
                            stepsCnt = stepsCnt + step.stepCount.toInt()
                        }
                    }
                    "cal" -> {
                        for (step in dteps){
                            stepsCnt = stepsCnt + step.cal.toInt()
                        }
                    }
                    "dist" -> {
                        for (step in dteps){
                            stepsCnt = stepsCnt + step.distance.toDouble()
                        }
                    }
                }
            }
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return  stepsCnt.toFloat()
    }

    fun getHeartRateCount(fromnumber: Double, toNumber: Double, type: String) :Float {

        val dataBaseHelper = DataBaseHelper(activity!!)
        var stepsCnt = 0
        try {
            val dteps = dataBaseHelper.getAllHeartRate("WHERE  date is DATE('"+ BaseHelper.parseDate(
                Date(), Constants.DATE_JSON)+"') AND time >= CAST ('"+fromnumber+"' as decimal) AND  time < CAST ('"+toNumber+"' " +
                    "as decimal) ORDER BY time DESC" )
            if (dteps.size > 0) {
                for (step in dteps){
                    stepsCnt = stepsCnt + step.heartRate
                }
                stepsCnt = stepsCnt/dteps.size
            }
        } catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return  stepsCnt.toFloat()
    }


}