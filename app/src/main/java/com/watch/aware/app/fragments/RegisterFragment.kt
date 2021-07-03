package com.watch.aware.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.webservices.PostRegisterViewModel
import kotlinx.android.synthetic.main.fragment_register.*
import androidx.lifecycle.ViewModelProviders
import com.iapps.libs.helpers.BaseHelper
import com.szabh.smable3.component.BleCache
import com.watch.aware.app.helper.Helper


class RegisterFragment : BaseFragment(),View.OnClickListener {
    lateinit var postRegisterViewModel: PostRegisterViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_register, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRegisterAPIObserver()
        register.setOnClickListener(this)
        signin.setOnClickListener(this)
        back.setOnClickListener(this)
    }

    fun validate() :Boolean{
        if(BaseHelper.isEmpty(username.text.toString())) {
            error_name.visibility = View.VISIBLE
            error_passowrd.visibility = View.GONE
            error_confirm_passowrd.visibility = View.GONE
            error_email.visibility = View.GONE
            username.requestFocus()
            return false
        } else if(BaseHelper.isEmpty(password.text.toString()) || password.text.toString().length < 8) {
            error_name.visibility = View.GONE
            error_passowrd.visibility = View.VISIBLE
            error_confirm_passowrd.visibility = View.GONE
            error_email.visibility = View.GONE
            password.requestFocus()
            return false
        }  else if(!password.text.toString().equals(confirm_password.text.toString())) {
            error_name.visibility = View.GONE
            error_passowrd.visibility = View.GONE
            error_confirm_passowrd.visibility = View.VISIBLE
            error_email.visibility = View.GONE
            confirm_password.requestFocus()
            return false
        } else if(!Helper.isValidEmail(email.text.toString())) {
            error_name.visibility = View.GONE
            error_passowrd.visibility = View.GONE
            error_confirm_passowrd.visibility = View.GONE
            error_email.visibility = View.VISIBLE
            email.requestFocus()
            return false
        }
        return true
    }
    fun setRegisterAPIObserver() {
        postRegisterViewModel = ViewModelProviders.of(this).get(PostRegisterViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                        PostRegisterViewModel.NEXT_STEP -> {
                            home()?.setFragment(RegistrationSuccessFragment())
                        }
                    }
                })

            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.register ->{
                if(validate()) {
                    var deviceID = ""
                    if(BleCache.mDeviceInfo != null ) {
                        deviceID = BleCache.mDeviceInfo?.mBleAddress!!
                    }
                    postRegisterViewModel.loadData(username.text.toString(),password.text.toString(),"",email.text.toString(),
                        deviceID)
                }

            }
            R.id.back -> {
                home()?.proceedDoOnBackPressed()
            }
            R.id.signin -> {
                home()?.setFragment(LoginFragment())
            }
        }
    }

}