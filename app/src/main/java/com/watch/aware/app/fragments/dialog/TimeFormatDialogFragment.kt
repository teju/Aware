package com.watch.aware.app.fragments.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.helper.Constants
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.dialog_time_format.*

class TimeFormatDialogFragment : DialogFragment() ,View.OnClickListener{
    var button_positive = ""
    @JvmField
    var button_negative = ""
    @JvmField
    var listener: NotifyListener? = null
    private var v: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.dialog_time_format, container, false)
        return v
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog!!.window
        window!!.setBackgroundDrawableResource(R.color.transparent)
        window.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20))

        if(UserInfoManager.getInstance(activity!!).getTimeFormat() == Constants.TWELVE_HOUR_FORMAT) {
            twenty_four_tick.visibility = View.VISIBLE
            twelve_tick.visibility = View.GONE
        } else {
            twenty_four_tick.visibility = View.GONE
            twelve_tick.visibility = View.VISIBLE
        }
        twenty_fout_hr.setOnClickListener {
            twenty_four_tick.visibility = View.VISIBLE
            twelve_tick.visibility = View.GONE
            dismiss()
            UserInfoManager.getInstance(activity!!).saveTimeFormat(Constants.TWENTY_HOUR_FORMAT)
            Constants.TIMEFORMAT =
                com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_JSON_HM

        }
        twelve_hr.setOnClickListener{
            twenty_four_tick.visibility = View.GONE
            twelve_tick.visibility = View.VISIBLE
            dismiss()
            UserInfoManager.getInstance(activity!!).saveTimeFormat(Constants.TWELVE_HOUR_FORMAT)
            Constants.TIMEFORMAT = com.iapps.logs.com.pascalabs.util.log.helper.Constants.TIME_hM

        }
    }


    override fun onClick(v: View?) {

    }
}