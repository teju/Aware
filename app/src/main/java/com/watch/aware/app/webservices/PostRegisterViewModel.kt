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

class PostRegisterViewModel(application: Application) : BaseViewModel(application) {

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

    fun loadData(username: String,password:String,email:String) {
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

                    try {
                        val gson = GsonBuilder().create()
                        obj = gson.fromJson(response!!.content.toString(), GenericResponse::class.java)
                        if (!Helper.isEmpty(obj!!.result)) {
                            trigger.postValue(NEXT_STEP)
                        }else{
                            trigger.postValue(ERROR)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        trigger.postValue(UnknownError)
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.postRegister)
        genericHttpAsyncTask.setPostParams(Keys.username,username)
        genericHttpAsyncTask.setPostParams(Keys.password,password)
        genericHttpAsyncTask.setPostParams(Keys.email,email)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
        var ERROR: Integer? = Integer(2)
        var UnknownError: Integer? = Integer(3)
    }

}