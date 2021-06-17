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

class PostSaveDeviceDataViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: GenericResponse? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(SPO2: String,HR:String,Temp:String,cough:String,deviceId:String,activity:String,timestamp:String) {
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
                    trigger.postValue(NEXT_STEP)

                    /*try {
                        val gson = GsonBuilder().create()
                        obj = gson.fromJson(response!!.content.toString(), GenericResponse::class.java)
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                            trigger.postValue(NEXT_STEP)
                        }else{
                            errorMessage.value = createErrorMessageObject(response)

                        }
                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }*/
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.postSaveDeviceData)
        genericHttpAsyncTask.setPostParams(Keys.SPO2,SPO2)
        genericHttpAsyncTask.setPostParams(Keys.HR,HR)
        genericHttpAsyncTask.setPostParams(Keys.Temp,Temp)
        genericHttpAsyncTask.setPostParams(Keys.cough,cough)
        genericHttpAsyncTask.setPostParams(Keys.activity,activity)
        genericHttpAsyncTask.setPostParams(Keys.deviceId,deviceId)
        genericHttpAsyncTask.setPostParams(Keys.timestamp,timestamp)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}