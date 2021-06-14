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
import com.watch.aware.app.helper.Helper

class NotifyDialogFragment : DialogFragment() {
    @JvmField
    var notify_tittle = ""
    @JvmField
    var notify_messsage = ""
    @JvmField
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
        v = inflater.inflate(R.layout.dialog_notify, container, false)
        return v
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog!!.window
        window!!.setBackgroundDrawableResource(R.color.transparent)
        window.setBackgroundDrawable(
            InsetDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                ), 20
            )
        )
        val vw_title =
            v!!.findViewById<View>(R.id.vw_text) as TextView
        val vw_text =
            v!!.findViewById<View>(R.id.vw_title) as TextView
        val btn_positive =
            v!!.findViewById<View>(R.id.btn_positive) as Button
        val btn_negative =
            v!!.findViewById<View>(R.id.btn_negative) as Button
        if (Helper.isEmpty(notify_tittle)) {
            vw_title.visibility = View.GONE
        } else {
            vw_title.visibility = View.VISIBLE
            vw_title.text = notify_tittle
        }
        if (!Helper.isEmpty(notify_messsage)) {
            vw_text.visibility = View.VISIBLE
            vw_text.text = notify_messsage
        }
        btn_positive.text = button_positive
        btn_negative.text = button_negative
        if (btn_negative.text !== "") {
            btn_negative.visibility = View.VISIBLE
        } else {
            btn_negative.visibility = View.GONE
        }
        btn_positive.setOnClickListener {
            if (listener != null) {
                listener!!.onButtonClicked(BUTTON_POSITIVE)
                dismiss()
            }
        }
        btn_negative.setOnClickListener {
            if (listener != null) {
                listener!!.onButtonClicked(BUTTON_NEGATIVE)
                dismiss()
            }
        }
    }

    companion object {
        @JvmField
        var TAG = "NotifyDialogFragment"
        var BUTTON_POSITIVE = 1
        var BUTTON_NEGATIVE = 0
    }
}