package com.watch.aware.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.UserInfoManager
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() ,View.OnClickListener{
    // TODO: Rename and change types of parameters

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_settings, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profile.setOnClickListener(this)
        settings.setOnClickListener(this)
        UserInfoManager.getInstance(activity!!).saveIsFirstTime(false)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.profile -> {
                home()?.setFragment(ProfileUpdateFragment())
            }
            R.id.settings -> {
                if(llsettings.visibility == View.VISIBLE) {
                    llsettings.visibility = View.GONE
                    chevron.setImageDrawable(activity?.resources?.getDrawable(R.drawable.chevron_right))
                } else {
                    llsettings.visibility = View.VISIBLE
                    chevron.setImageDrawable(activity?.resources?.getDrawable(R.drawable.chevron_down))
                }
            }
        }
    }
}