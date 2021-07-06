package com.watch.aware.app.fragments.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import com.bestmafen.baseble.scanner.BleDevice
import com.watch.aware.app.R
import com.watch.aware.app.callback.DeviceItemClickListener
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.DeviceListAdapter
import kotlinx.android.synthetic.main.device_listing.*
import java.util.*

class DeviceListingDialogFragment : DialogFragment() {
    var arrayList = ArrayList<BleDevice>()

    var listener: DeviceItemClickListener? = null
    private var v: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.device_listing, container, false)
        return v
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog!!.window
        window!!.setBackgroundDrawableResource(R.color.transparent)
        window.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 0))

        val customAdapter = DeviceListAdapter(activity!!, arrayList)
        device_list.adapter = customAdapter
        device_list.setOnItemClickListener(object  : AdapterView.OnItemClickListener{
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                listener?.onItemClick(position)
                dismiss()
            }
        })
    }

    companion object {
        @JvmField
        var TAG = "NotifyDialogFragment"
        var BUTTON_POSITIVE = 1
        var BUTTON_NEGATIVE = 0
    }
}