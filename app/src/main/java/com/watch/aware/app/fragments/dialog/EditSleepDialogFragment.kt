package com.watch.aware.app.fragments.dialog

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_hM
import com.watch.aware.app.R
import com.watch.aware.app.callback.EditSlotsListener
import com.watch.aware.app.helper.Constants
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.edit_slots_dialog.*
import org.joda.time.format.ISODateTimeFormat.hour
import java.util.*


class EditSleepDialogFragment : DialogFragment(),TimePicker.OnTimeChangedListener {
    var listener: EditSlotsListener? = null
    private var v: View? = null
    private val resendOtp: TextView? = null
    var from = 8
    var to = 12
    var range = 4
    private var mFromHourPicker: NumberPicker? = null
    private var mToHourPicker: NumberPicker? = null
    private var mFromMinutePicker: NumberPicker? = null
    private var mToMinutePicker: NumberPicker? = null
    var timeType = 0
    val START_TIME = 0
    val END_TIME = 1
    var hour = 0
    var minutes = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.edit_slots_dialog, container, false)
        return v
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog!!.window
        window!!.setGravity(Gravity.BOTTOM)
        val lp = WindowManager.LayoutParams()
        lp.windowAnimations = R.style.DialogAnimation
        lp.gravity = Gravity.BOTTOM
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
        window.setBackgroundDrawableResource(R.color.transparent)

        val cancel =
            v!!.findViewById<View>(R.id.cancel) as TextView
        val tv_end_time =
            v!!.findViewById<View>(R.id.tv_end_time) as TextView
        val tv_start_time =
            v!!.findViewById<View>(R.id.tv_start_time) as TextView
        val done =
            v!!.findViewById<View>(R.id.done) as TextView
        val to_time =
            v!!.findViewById<View>(R.id.to_time) as TimePicker
        if(UserInfoManager.getInstance(activity!!).getTimeFormat() == Constants.TWENTY_HOUR_FORMAT) {
            to_time.setIs24HourView(true)
        } else {
            to_time.setIs24HourView(false)
        }
        to_time.setOnTimeChangedListener(this)
        llEndTime.setOnClickListener {
            timeType = END_TIME
            llStartTime.setBackgroundColor(
                activity!!.resources.getColor(R.color.light_gray))

            llEndTime.setBackgroundColor(
                activity!!.resources.getColor(R.color.colorPrimary))

        }
        llStartTime.setOnClickListener {
            timeType = START_TIME
            llStartTime.setBackgroundColor(
                activity!!.resources.getColor(R.color.colorPrimary))
            llEndTime.setBackgroundColor(
                activity!!.resources.getColor(R.color.light_gray))

        }
        cancel.setOnClickListener { dismiss() }
        done.setOnClickListener {
            dismiss()
        }
    }
    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hour = hourOfDay
        minutes = minute
        var timeSet = ""
        if (hour > 12) {
            hour -= 12
            timeSet = "PM"
        } else if (hour === 0) {
            hour += 12
            timeSet = "AM"
        } else if (hour === 12) {
            timeSet = "PM"
        } else {
            timeSet = "AM"
        }
        var min: String? = ""
        if (minutes < 10) min = "0$minutes" else min = java.lang.String.valueOf(minutes)

        // Append in a StringBuilder
        val aTime = StringBuilder().append(hour).append(':')
            .append(min).append(" ").append(timeSet).toString()
        val mtime = BaseHelper.parseDate(aTime,TIME_hM)
        val displayTime  = BaseHelper.parseDate(mtime,Constants.TIMEFORMAT)
        if(timeType == START_TIME) {
            tv_start_time.setText(displayTime)
            UserInfoManager.getInstance(activity!!).saveSleepStartTime(aTime)
        } else {
            UserInfoManager.getInstance(activity!!).saveSleepEndTime(aTime)
            tv_end_time.setText(displayTime)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NO_TITLE,
            R.style.DialogStyle
        )
    }

    companion object {
        var TAG = "NotifyDialogFragment"
    }

}

