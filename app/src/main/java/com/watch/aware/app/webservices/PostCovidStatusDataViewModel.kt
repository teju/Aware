package com.watch.aware.app.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.watch.aware.app.helper.APIs
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.Keys
import com.watch.aware.app.helper.SingleLiveEvent
import com.watch.aware.app.models.GenericResponse
import com.watch.aware.app.models.covid_status.CovidStatus
import java.lang.Exception

class PostCovidStatusDataViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: CovidStatus? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(deviceId:String) {
        genericHttpAsyncTask = Helper.GenericHttpAsyncTask(object : Helper.GenericHttpAsyncTask.TaskListener {

            override fun onPreExecute() {
                isLoading.postValue(true)
            }

            override fun onPostExecute(response: Response?) {
                isLoading.postValue(false)

                if (!Helper.isNetworkAvailable(apl)) {
                    isNetworkAvailable.postValue(false)
                    return
                }

                val json = checkResponse(response, apl)

                if (response != null) {
                    trigger.postValue(PostRegisterViewModel.NEXT_STEP)

                    try {
                        val gson = GsonBuilder().create()
                        obj = gson.fromJson(response!!.content.toString(), CovidStatus::class.java)
                        if (!Helper.isEmpty(obj!!.CovidPrediction)) {
                            trigger.postValue(NEXT_STEP)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        trigger.postValue(PostRegisterViewModel.UnknownError)
                        //showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.postGetCovidStatus)
        try {
            genericHttpAsyncTask.setPostParams(Keys.deviceId, deviceId)
        }catch (e:Exception){

        }
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}