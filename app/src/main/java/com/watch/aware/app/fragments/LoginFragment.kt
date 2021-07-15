package com.watch.aware.app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.libs.helpers.BaseHelper
import com.szabh.smable3.component.BleCache
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.helper.UserInfoManager
import com.watch.aware.app.webservices.PostLoginViewModel
import com.watch.aware.app.webservices.PostRegisterViewModel
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BaseFragment(),View.OnClickListener {
    lateinit var postLoginFragment: PostLoginViewModel

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
        setRegisterAPIObserver()
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
                if(validate()) {
                    postLoginFragment.loadData(password.text.toString(),email.text.toString())
                }
            }
            R.id.forget_password -> {
                home()?.setFragment(ResetPasswordFragment())
            }
        }
    }
    fun validate() :Boolean{
        if(!Helper.isValidEmail(email.text.toString())) {
            error_passowrd.visibility = View.GONE
            error_email.visibility = View.VISIBLE
            email.requestFocus()
            return false
        } else if(BaseHelper.isEmpty(password.text.toString()) || password.text.toString().length < 8) {
            error_passowrd.visibility = View.VISIBLE
            error_email.visibility = View.GONE
            password.requestFocus()
            return false
        }
        return true
    }

    fun setRegisterAPIObserver() {
        postLoginFragment = ViewModelProviders.of(this).get(PostLoginViewModel::class.java).apply {
            this@LoginFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet as Observer<in Boolean>)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostLoginViewModel.NEXT_STEP -> {
                            home()?.setFragment(RegistrationSuccessFragment())
                            if(obj?.Name != null) {
                                UserInfoManager.getInstance(activity!!).saveAccountName(obj?.Name!!)
                            }
                            UserInfoManager.getInstance(activity!!).saveEmail(email.text.toString())
                            UserInfoManager.getInstance(activity!!).saveIsLoggedIn(true)
                            UserInfoManager.getInstance(activity!!).saveIsFirstTime(true)
                            if(BleCache.mDeviceInfo == null) {
                                home()?.setFragment(ConnectionFragment())
                            } else{
                                home()?.setFragment(CoughSettingsFragment())
                            }
                        }
                        PostRegisterViewModel.ERROR -> {
                            showNotifyDialog(
                                "", obj?.errorDesc,
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) {

                                    }
                                }
                            )
                        }
                    }
                })
            }
        }
    }


}