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
import com.iapps.libs.helpers.BaseHelper
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.helper.Constants
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.edit_goal.*

class EditGoalDialogFragment : DialogFragment() ,View.OnClickListener{

    private var v: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.edit_goal, container, false)
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
        steps.setOnClickListener(this)
        cal.setOnClickListener(this)
        dist.setOnClickListener(this)
        done.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.steps -> {
                steps.isChecked = true
                cal.isChecked = false
                dist.isChecked = false
                UserInfoManager.getInstance(activity!!).saveGoalType(Constants.STEPS_GOAL)
            }
            R.id.cal -> {
                steps.isChecked = false
                cal.isChecked = true
                dist.isChecked = false
                UserInfoManager.getInstance(activity!!).saveGoalType(Constants.CAL_GOAL)

            }
            R.id.dist -> {
                steps.isChecked = false
                cal.isChecked = false
                dist.isChecked = true
                UserInfoManager.getInstance(activity!!).saveGoalType(Constants.DIST_GOAL)

            }
            R.id.done -> {
                if(!BaseHelper.isEmpty(goal_value.text.toString())) {
                    UserInfoManager.getInstance(activity!!).saveGoalValue(goal_value.text.toString().toInt())
                    dismiss()
                }
            }
        }
    }
    companion object {
        @JvmField
        var TAG = "EditGoalDialogFragment"
    }
}