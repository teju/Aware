package com.watch.aware.app.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.watch.aware.app.helper.*
import com.watch.aware.app.models.GenericResponse

class PostUpdateProfileModel(application: Application) : BaseViewModel(application) {

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

    fun loadData(
        username: String,
        age: String,
        email: String,
        contactNumber: String,
        gender: String,
        mBleAddress: String
    ) {
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
        genericHttpAsyncTask.setUrl(APIs.postProfileUpdate)
        if(!Helper.isEmpty(username)) {
            genericHttpAsyncTask.setPostParams(Keys.username,username)
        }
        if(!Helper.isEmpty(age)) {
            genericHttpAsyncTask.setPostParams(Keys.age,age)
        }
        if(!Helper.isEmpty(email)) {
            genericHttpAsyncTask.setPostParams(Keys.email,email)
        }
        if(!Helper.isEmpty(contactNumber)) {
            genericHttpAsyncTask.setPostParams(Keys.contactNumber,contactNumber)
        }
        if(!Helper.isEmpty(gender)) {
            genericHttpAsyncTask.setPostParams(Keys.gender,gender)
        }
        genericHttpAsyncTask.setPostParams(Keys.deviceId, UserInfoManager.getInstance(apl).getDeviceID())

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