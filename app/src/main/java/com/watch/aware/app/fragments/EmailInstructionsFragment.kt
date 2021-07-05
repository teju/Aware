package com.watch.aware.app.fragments

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import kotlinx.android.synthetic.main.fragment_email_instructions.*

class EmailInstructionsFragment : BaseFragment(),View.OnClickListener {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_email_instructions, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        open_mail.setOnClickListener(this)
        skip.setOnClickListener(this)
        tvretry.setText(Html.fromHtml("Did not receive the email? Check your spem filter, or <b><font color='#02FE97'>try another email address</font></b>"))
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.open_mail -> {

            }
            R.id.skip -> {
                home()?.setFragment(CreateNewPasswordFragment())
            }
        }
    }
}