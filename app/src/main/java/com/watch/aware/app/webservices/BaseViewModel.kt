package com.watch.aware.app.webservices

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.helpers.BaseKeys
import com.iapps.libs.objects.Response
import com.watch.aware.app.helper.Keys
import com.watch.aware.app.helper.SingleLiveEvent

import org.json.JSONObject
import java.net.HttpURLConnection

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    var isLoading = SingleLiveEvent<Boolean>()
    var isOauthExpired = SingleLiveEvent<Boolean>()
    var isNetworkAvailable = SingleLiveEvent<Boolean>()
    var isMaintenance = SingleLiveEvent<String>()
    var errorMessage = SingleLiveEvent<ErrorMessageModel>()

    inner class ErrorMessageModel {
        var isShouldDisplayDialog: Boolean = false
        var title: String? = null
        var message: String? = null
    }

    fun checkResponse(
        response: Response?,
        context: Context
    ): JSONObject? {

        val errorMessageModel = ErrorMessageModel()

        if (response != null) {
            val json = response.content

            if (response.statusCode == BaseConstants.STATUS_NOT_FOUND) {
                return json
            }

            if (response.statusCode == BaseConstants.STATUS_BAD_REQUEST) {
                try {
                    if(json != null){
                        return json
                    }
                } catch (e: Exception) {
                    showUnknowResponseErrorMessage(response.statusCode.toString())
                }
                return null
            }

            if (response.statusCode == HttpURLConnection.HTTP_UNAVAILABLE) {
                isNetworkAvailable.postValue(false)
                return null
            }

            if (response.statusCode == BaseConstants.STATUS_SUCCESS) {
                return json
            } else if (response.statusCode == BaseConstants.STATUS_TIMEOUT) {
                errorMessageModel.message = context.getString(com.iapps.common_library.R.string.iapps__conn_timeout)
                errorMessage.postValue(errorMessageModel)
            } else if (response.statusCode == BaseConstants.STATUS_NO_CONNECTION) {
                errorMessageModel.message = context.getString(com.iapps.common_library.R.string.iapps__conn_fail)
                errorMessage.postValue(errorMessageModel)
            } else {

                try {
                    if (response.statusCode == 403) {
                        isOauthExpired.postValue(true)
                        return null
                    }else{
                        try {
                            val statusCode = json.getString(BaseKeys.STATUS_CODE)
                            showUnknowResponseErrorMessage(statusCode)
                        } catch (e: Exception) {
                            showUnknowResponseErrorMessage(response.statusCode.toString())
                        }
                    }
                } catch (e: Exception) {
                    showUnknowResponseErrorMessage()
                }

            }
        } else {

            showUnknowResponseErrorMessage()
        }

        return null
    }


    fun showUnknowResponseErrorMessage() {
         /* errorMessage.postValue(createErrorMessageObject(
          false,
            getApplication<Application>().getString(com.iapps.common_library.R.string.iapps__network_error),
            getApplication<Application>().getString(com.iapps.common_library.R.string.iapps__unknown_response)
        ))*/
    }

    fun showUnknowResponseErrorMessage(errorStatusCode: String) {
       /* errorMessage.postValue(createErrorMessageObject(
            false,
            getApplication<Application>().getString(com.iapps.common_library.R.string.iapps__network_error),
            String.format("%s (%s)", getApplication<Application>().getString(com.iapps.common_library.R.string.iapps__unknown_response), errorStatusCode)
        ))*/
    }

    protected fun createErrorMessageObject(displayDialog: Boolean, title: String, message: String): ErrorMessageModel {
        val errorMessageModel = ErrorMessageModel()
        errorMessageModel.isShouldDisplayDialog = displayDialog
        errorMessageModel.title = title
        errorMessageModel.message = message
        return errorMessageModel
    }

    protected fun createErrorMessageObject(response: Response?): ErrorMessageModel {
        try {
            val errorMessageModel = ErrorMessageModel()
            errorMessageModel.isShouldDisplayDialog = true
            errorMessageModel.title = ""
            errorMessageModel.message = response!!.content.getString(Keys.MESSAGE)
            return errorMessageModel
        } catch (e: Exception) {
            val errorMessageModel = ErrorMessageModel()
            errorMessageModel.isShouldDisplayDialog = false
            errorMessageModel.title = ""
            errorMessageModel.message = "No Data Found"
            return errorMessageModel
        }
    }

    companion object {
        var isOauthExpiredSeamlessLogin: SingleLiveEvent<Boolean>? = null
    }

}