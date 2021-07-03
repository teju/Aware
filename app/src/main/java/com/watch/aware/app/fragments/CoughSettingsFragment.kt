package com.watch.aware.app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import kotlinx.android.synthetic.main.fragment_cough_settings.*


class CoughSettingsFragment : BaseFragment() ,View.OnClickListener{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_cough_settings, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnno.setOnClickListener(this)
        btnyes.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btnno -> {
                home()?.setFragment(MainTabFragment())
            }
            R.id.btnyes -> {
                home()?.setFragment(RecordCoughFragment())
            }
        }
    }

}