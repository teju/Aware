package com.watch.aware.app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.szabh.smable3.component.BleCache
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : BaseFragment(),View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_login, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signup.setOnClickListener(this)
        login.setOnClickListener(this)
        forget_password.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.signup -> {
                home()?.setFragment(RegisterFragment())
            }
            R.id.login -> {
                if(BleCache.mBleName == null) {
                    home()?.setFragment(ConnectionFragment())
                } else{
                    home()?.setFragment(CoughSettingsFragment())
                }
            }
            R.id.forget_password -> {
                home()?.setFragment(ResetPasswordFragment())
            }
        }
    }


}