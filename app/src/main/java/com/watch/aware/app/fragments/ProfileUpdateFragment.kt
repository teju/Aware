package com.watch.aware.app.fragments

import android.os.Bundle
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
import com.watch.aware.app.webservices.PostRegisterViewModel
import com.watch.aware.app.webservices.PostUpdateProfileModel
import kotlinx.android.synthetic.main.fragment_profile_update.*


class ProfileUpdateFragment : BaseFragment() {

    lateinit var postUpdateProfileModel: PostUpdateProfileModel
    var gender = "M"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_profile_update, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpdateProfileAPIObserver()
        genderSettings()
        setUSerData()
        save.setOnClickListener {
            if(validate()) {
                Helper.hideKeyboard(activity!!)
                var deviceAddress = ""
                if(BleCache.mDeviceInfo != null) {
                    deviceAddress = BleCache.mDeviceInfo?.mBleAddress!!
                }
                postUpdateProfileModel.loadData(username.text.toString(),age.text.toString(),
                    email.text.toString(),contact_number.text.toString(),gender,deviceAddress )
            }
        }
        back.setOnClickListener {
            home()?.proceedDoOnBackPressed()
        }
    }

    fun setUSerData() {
        username.setText(UserInfoManager.getInstance(activity!!).getAccountName())
        email.setText(UserInfoManager.getInstance(activity!!).getEmail())
        contact_number.setText(UserInfoManager.getInstance(activity!!).getContactNumber())
        age.setText(UserInfoManager.getInstance(activity!!).getAge())
        if(UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
            male.isChecked = false
            female.isChecked = true
        } else{
            male.isChecked = true
            female.isChecked = false
        }
    }
    fun validate() :Boolean {
        if (BaseHelper.isEmpty(username.text.toString())) {
            error_name.visibility = View.VISIBLE

            error_email.visibility = View.GONE
            username.requestFocus()
            return false
        } else if(!Helper.isValidEmail(email.text.toString())) {
            error_name.visibility = View.GONE
            error_email.visibility = View.VISIBLE
            email.requestFocus()
            return false
        }
        return true
    }
        fun genderSettings() {
        male.setOnClickListener {
            gender = "M"
            male.isChecked = true
            female.isChecked = false

        }
        female.setOnClickListener {
            gender = "F"

            male.isChecked = false
            female.isChecked = true

        }
    }
    fun setUpdateProfileAPIObserver() {
        postUpdateProfileModel = ViewModelProviders.of(this).get(PostUpdateProfileModel::class.java).apply {
            this@ProfileUpdateFragment.let { thisFragReference ->
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
                            UserInfoManager.getInstance(activity!!).saveAccountName(username.text.toString())
                            UserInfoManager.getInstance(activity!!).saveEmail(email.text.toString())
                            UserInfoManager.getInstance(activity!!).saveAge(age.text.toString())
                            UserInfoManager.getInstance(activity!!).saveContactNumber(contact_number.text.toString())
                            if(male.isChecked) {
                                UserInfoManager.getInstance(activity!!).saveGender("M")
                            } else {
                                UserInfoManager.getInstance(activity!!).saveGender("F")
                            }
                            home()?.proceedDoOnBackPressed()
                        }
                        PostRegisterViewModel.ERROR -> {
                            showNotifyDialog(
                                "", obj?.errorDesc,
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) { }
                                }
                            )
                        }
                    }
                })
            }
        }
    }

}