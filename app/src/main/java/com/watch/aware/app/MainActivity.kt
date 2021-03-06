package com.watch.aware.app

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.MainTabFragment
import com.watch.aware.app.fragments.dialog.NotifyDialogFragment
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Helper
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var MAIN_FLOW_INDEX = 0
    var MAIN_FLOW_TAG = "MainFlowFragment"
    private var currentFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_main)
        Handler().postDelayed({
            logo_icon.setVisibility(View.GONE)
            triggerMainProcess()
        }, 2 * 2000.toLong())
    }

    fun triggerMainProcess() {
        setFragment(MainTabFragment())
        showNotifyDialog("","Please make sure your GPS is turned on",
            "OK","",object :NotifyListener{
                override fun onButtonClicked(which: Int) {

                }

            })
    }

    fun clearFragment() {
        supportFragmentManager.popBackStack(
            MAIN_FLOW_TAG,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        for (i in MAIN_FLOW_INDEX downTo 0) {
            try {
                val fragment =
                    supportFragmentManager.findFragmentByTag(MAIN_FLOW_TAG + i)
                if (fragment != null) supportFragmentManager.beginTransaction()
                    .remove(fragment).commitNowAllowingStateLoss()
            } catch (e: Exception) {
            }
        }
        supportFragmentManager.popBackStack(
            "MAIN_TAB",
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        MAIN_FLOW_INDEX = 0
    }

    fun getcurrentFragment() {
        currentFragment = supportFragmentManager.findFragmentById(R.id.layoutFragment)
    }

    fun setFragment(fragment: Fragment?) {
        try {
            val f =
                supportFragmentManager.beginTransaction()
            val list =
                supportFragmentManager.fragments
            for (frag in list) {
                if (frag.isVisible) {
                    f.hide(frag)
                }
            }
            MAIN_FLOW_INDEX = MAIN_FLOW_INDEX + 1
            f.add(R.id.layoutFragment, fragment!!, MAIN_FLOW_TAG + MAIN_FLOW_INDEX).addToBackStack(
                MAIN_FLOW_TAG
            ).commitAllowingStateLoss()
            Helper.hideKeyboard(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetAndGoToFragment(frag: Fragment?) {
        clearFragment()
        setFragment(frag)
    }

    fun resetAndExit() {
        showNotifyDialog("Are you sure you want to exit ?",
            "", "OK",
            "Cancel", object : NotifyListener {
                override fun onButtonClicked(which: Int) {
                    if (which == NotifyDialogFragment.BUTTON_POSITIVE) {
                        clearFragment()
                        finish()
                    }
                }
            } as NotifyListener
        )
    }

    fun backToMainScreen() {
        try {
            clearFragment()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            setFragment(MainTabFragment())
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        val f =
            supportFragmentManager.beginTransaction()
        val list =
            supportFragmentManager.fragments
        var foundVisible = false
        for (i in list.indices) {
            if (list[i].isVisible) {
                if (list[i] is BaseFragment) {
                    foundVisible = true
                    (list[i] as BaseFragment).onBackTriggered()
                }
            }
        }
        if (!foundVisible) proceedDoOnBackPressed()
    }

    fun proceedDoOnBackPressed() {
        Helper.hideKeyboard(this)
        val f =
            supportFragmentManager.beginTransaction()
        val list =
            supportFragmentManager.fragments
        try {
            for (frag in list) {
                if (frag.tag!!.contentEquals(MAIN_FLOW_TAG + (MAIN_FLOW_INDEX - 1))) {
                    f.show(frag)
                }
            }
        } catch (e: Exception) {
        }
        if (supportFragmentManager.backStackEntryCount <= 1
            || currentFragment is MainTabFragment
        ) {
            finish()
        } else {
            super.onBackPressed()
        }
        MAIN_FLOW_INDEX = MAIN_FLOW_INDEX - 1
    }

    fun setOrShowExistingFragmentByTag(
        layoutId: Int,
        fragTag: String?,
        backstackTag: String?,
        newFrag: Fragment?,
        listFragmentTagThatNeedToHide: ArrayList<String>
    ) {
        var foundExistingFragment = false
        val fragment =
            supportFragmentManager.findFragmentByTag(fragTag)
        val transaction =
            supportFragmentManager.beginTransaction()
        if (fragment != null) {
            for (i in supportFragmentManager.fragments.indices) {
                try {
                    val f =
                        supportFragmentManager.fragments[i]
                    for (tag in listFragmentTagThatNeedToHide) {
                        try {
                            if (tag.toLowerCase() == tag.toLowerCase()) {
                                transaction.hide(f)
                            }
                        } catch (e: Exception) {
                        }
                    }
                } catch (e: Exception) {
                }
            }
            try {
                transaction.show(fragment).commit()
            } catch (e: Exception) {
                try {
                    transaction.show(fragment).commitAllowingStateLoss()
                } catch (e1: Exception) {
                }
            }
            foundExistingFragment = true
        }
        if (foundExistingFragment) {
            val _fragment =
                supportFragmentManager.findFragmentByTag(fragTag)
            if (_fragment != null) supportFragmentManager.beginTransaction().remove(_fragment)
                .commit()
        }
        setFragmentInFragment(layoutId, newFrag, fragTag, backstackTag)
    }

    fun setFragmentInFragment(
        fragmentLayout: Int,
        frag: Fragment?,
        tag: String?,
        backstackTag: String?
    ) {
        try {
            supportFragmentManager.beginTransaction().replace(fragmentLayout, frag!!, tag)
                .addToBackStack(backstackTag)
                .commit()
            Helper.hideKeyboard(this)
        } catch (e: Exception) {
            try {
                supportFragmentManager.beginTransaction().replace(fragmentLayout, frag!!, tag)
                    .addToBackStack(backstackTag)
                    .commitAllowingStateLoss()
                Helper.hideKeyboard(this)
            } catch (e1: Exception) {
            }
        }
    }

    fun showNotifyDialog(
        tittle: String?,
        messsage: String?,
        button_positive: String?,
        button_negative: String?,
        n: NotifyListener?
    ) {
        val f = NotifyDialogFragment()
        f.listener = n
        f.notify_tittle = tittle!!
        f.notify_messsage = messsage!!
        f.button_positive = button_positive!!
        f.button_negative = button_negative!!
        f.setCancelable(false)
        if (!Helper.isEmpty(tittle) || !Helper.isEmpty(
                messsage
            )
        ) {
            f.show(supportFragmentManager, NotifyDialogFragment.TAG)
        }
    }
}