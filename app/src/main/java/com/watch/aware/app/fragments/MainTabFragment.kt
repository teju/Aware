package com.watch.aware.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.watch.aware.app.R
import com.watch.aware.app.helper.Helper
import com.watch.aware.app.fragments.settings.BaseFragment

class MainTabFragment : BaseFragment() {
    var bottomNavigation: BottomNavigationView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.main_tab_fragment, container, false)
        return v
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        bottomNavigation =
            v.findViewById(R.id.bottom_navigation)
        bottomNavigation!!.setOnNavigationItemSelectedListener(navigationItemSelectedListener)
        val homeFragment = WelnessFragment()
        homeFragment.setBottomNavigation(bottomNavigation)
        home().setOrShowExistingFragmentByTag(
            R.id.mainLayoutFragment, "FIRST_TAB",
            "MAIN_TAB", homeFragment, Helper.listFragmentsMainTab()
        )
    }

    var navigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_welness -> {
                    val welness = WelnessFragment()
                    welness.setBottomNavigation(bottomNavigation)
                    home().setFragmentInFragment(
                        R.id.mainLayoutFragment, welness,
                        "MAIN_TAB", "FIRST_TAB"
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_fitness -> {
                    val fitness = FitnessFragment()
                    fitness.setBottomNavigation(bottomNavigation)
                    home().setFragmentInFragment(
                        R.id.mainLayoutFragment, fitness,
                        "MAIN_TAB", "FIRST_TAB"
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_goal ->{
                    val goalProgress = GoalProgressFragment()
                    goalProgress.setBottomNavigation(bottomNavigation)
                    home().setFragmentInFragment(
                        R.id.mainLayoutFragment, goalProgress,
                        "MAIN_TAB", "FIRST_TAB"
                    )
                    return@OnNavigationItemSelectedListener true

                }
                R.id.navigation_insight ->{
                    val insights = InsightsFragment()
                    insights.setBottomNavigation(bottomNavigation)
                    home().setFragmentInFragment(
                        R.id.mainLayoutFragment, insights,
                        "MAIN_TAB", "FIRST_TAB"
                    )
                    return@OnNavigationItemSelectedListener true

                }
                R.id.navigation_settings ->{
                    val settings = SettingsFragment()
                    settings.setBottomNavigation(bottomNavigation)
                    home().setFragmentInFragment(
                        R.id.mainLayoutFragment, settings,
                        "MAIN_TAB", "FIRST_TAB"
                    )
                    return@OnNavigationItemSelectedListener true

                }
            }
            false
        }

    override fun onBackTriggered() {
        home().resetAndExit();
    }
}