package com.watch.aware.app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment

class SettingsFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    fun setBottomNavigation(bottomNavigation: BottomNavigationView?) {
        this.bottomNavigation = bottomNavigation
    }
    private var bottomNavigation: BottomNavigationView? = null
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
    }

}