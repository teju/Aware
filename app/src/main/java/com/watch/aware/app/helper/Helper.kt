package com.watch.aware.app.helper

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.iapps.libs.helpers.HTTPAsyncTask
import com.iapps.libs.objects.Response
import com.iapps.logs.com.pascalabs.util.log.activity.ActivityPascaLog.getStackTrace

import com.watch.aware.app.R
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.random.Random

object Helper {
    open class GenericHttpAsyncTask(internal var taskListener: TaskListener?) : HTTPAsyncTask() {

        lateinit var nonce: String

        interface TaskListener {
            fun onPreExecute()
            fun onPostExecute(response: Response?)
        }

        override fun onPreExecute() {
            if (taskListener != null) taskListener!!.onPreExecute()
        }

        override fun onPostExecute(@Nullable response: Response?) {

            if (response == null) {

                var apiDetails = ""

                try {
                    apiDetails = apiDetails + this.url.path + "\n"
                } catch (e: Exception) {
                    logException(null, e)
                }

                try {
                    apiDetails = apiDetails + this.params.toString() + "\n"
                } catch (e: Exception) {
                    logException(null, e)
                }

                var rawResponse = ""
                try {
                    rawResponse = rawResponse + this.rawResponseString
                } catch (e: Exception) {
                    logException(null, e)
                }

            }

            if (taskListener != null) taskListener!!.onPostExecute(response)
        }

    }

    fun hideKeyboard(activity: Activity) {
        val imm =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun logException(ctx: Context?, e: Exception?) {
        try {
            if (Constants.IS_DEBUGGING) {
                if (Constants.IS_DEBUGGING) {
                    if (ctx != null)
                        Log.v(ctx.getString(R.string.app_name), getStackTrace(e!!))
                    else
                        print(getStackTrace(e!!))
                }
            }
        } catch (e1: Exception) {
        }
    }

    @JvmStatic
    fun isEmpty(string: String?): Boolean {
        return if (string == null || string.trim { it <= ' ' }.length == 0) {
            true
        } else false
    }

    fun dateFormat(format: String, date: Date?): String {
        val simpleDateFormat = SimpleDateFormat(format)
        return simpleDateFormat.format(date)
    }

    fun convertStringToDate(format: String?, date: String?): Date? {
        val dateFormat: DateFormat =
            SimpleDateFormat(format, Locale.ENGLISH)
        val parse: Date
        parse = try {
            dateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }
        return parse
    }

    fun findDifferenceBetweenDates(
        startDate: Date,
        endDate: Date
    ): Long {
        val duration = endDate.time - startDate.time
        return TimeUnit.MILLISECONDS.toDays(duration)
    }

    fun isNetworkAvailable(ctx: Context): Boolean {
        return try {
            val manager =
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo
            var isAvailable = false
            if (networkInfo != null && networkInfo.isConnected) {
                isAvailable = true
            }
            if (!isAvailable) {
            }
            isAvailable
        } catch (e: Exception) {
            true
        }
    }

    fun listFragmentsMainTab(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("FIRST_TAB")
        list.add("SECOND_TAB")
        list.add("THIRD_TAB")
        return list
    }

    fun isValidMobile(phone: String): Boolean {
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length == 10
        } else false
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(
            target
        ).matches()
    }

    fun visibleView(v: View?) {
        if (v != null) {
            v.visibility = View.VISIBLE
        }
    }

    fun goneView(v: View?) {
        if (v != null) {
            v.visibility = View.GONE
        }
    }



    private fun setCameraIntents(
        cameraIntents: MutableList<Intent>,
        output: Uri,
        context: Context
    ) {
        val captureIntent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = context.packageManager
        val listCam =
            packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val intent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(context.packageManager) != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output)
                cameraIntents.add(intent)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val isKitKat =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                try {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(
                        context,
                        contentUri,
                        null,
                        null
                    )
                } catch (e: Exception) {
                    val fileName =
                        getFilePath(context, uri)
                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory()
                            .toString() + "/Download/" + fileName
                    }
                    e.printStackTrace()
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun showImage(reg_real_Path: String?): Bitmap? {
        val imgFile = File(reg_real_Path)
        var myBitmap: Bitmap? = null
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        }
        return myBitmap
    }

    fun getCurrentDate(): String? {
        val outputFmt = SimpleDateFormat("yyyy-MM-dd hh:mm:ss zz")
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        val dateAsString = outputFmt.format(Date())
        return dateAsString
    }


}