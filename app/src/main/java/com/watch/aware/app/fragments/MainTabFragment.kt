package com.watch.aware.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.watch.aware.app.R
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.models.CbnMenuItem
import kotlinx.android.synthetic.main.main_tab_fragment.*

class MainTabFragment : BaseFragment() {
    var instance : Int = 0

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
        try {
            initNavigationView()
        } catch (e : Exception){

        }
    }
    fun  initNavigationView() {

        val menuItems = arrayOf(
            CbnMenuItem(
                R.string.welness,
                R.drawable.bluetooth, // the icon
                R.drawable.avd_bluetooth,// the AVD that will be shown in FAB
                R.id.navigation_welness // optional if you use Jetpack Navigation
            ),
            CbnMenuItem(
                R.string.fitness,
                R.drawable.bluetooth,
                R.drawable.avd_bluetooth,
                R.id.navigation_fitness
            ),
            CbnMenuItem(
                R.string.goal,
                R.drawable.bluetooth,
                R.drawable.avd_bluetooth,
                R.id.navigation_goal
            ),
            CbnMenuItem(
                R.string.insights,
                R.drawable.bluetooth,
                R.drawable.avd_bluetooth,
                R.id.navigation_insight
            ),
            CbnMenuItem(
                R.string.settings,
                R.drawable.bluetooth,
                R.drawable.avd_bluetooth,
                R.id.navigation_settings
            )
        )

        nav_view.setMenuItems(menuItems, instance)
        nav_view.onMenuItemClick(instance)
        setCurrentItem(R.id.navigation_welness)
        nav_view.setOnMenuItemClickListener { item, _ ->
            setCurrentItem(item.destinationId)
        }
        showTab()

    }
    open fun showTab() {
        activity?.runOnUiThread(object:Runnable {
            override fun run() {
                nav_view.onMenuItemClick(instance)
                setCurrentItem(instance)

            }
        })
    }


    fun setCurrentItem(which: Int) {
        when (which) {
            R.id.navigation_welness -> {
                val welness = WelnessFragment()
                home()?.setFragmentInFragment(
                    R.id.mainLayoutFragment, welness,
                    "MAIN_TAB", "FIRST_TAB"
                )

            }
            R.id.navigation_fitness -> {
                val fitness = FitnessFragment()
                home()?.setFragmentInFragment(
                    R.id.mainLayoutFragment, fitness,
                    "MAIN_TAB", "FIRST_TAB"
                )

            }
            R.id.navigation_goal ->{
                val goalProgress = GoalProgressFragment()
                home()?.setFragmentInFragment(
                    R.id.mainLayoutFragment, goalProgress,
                    "MAIN_TAB", "FIRST_TAB"
                )


            }
            R.id.navigation_insight ->{
                val insights = InsightsFragment()
                home()?.setFragmentInFragment(
                    R.id.mainLayoutFragment, insights,
                    "MAIN_TAB", "FIRST_TAB"
                )


            }
            R.id.navigation_settings ->{
                val settings =
                    SettingsFragment()
                home()?.setFragmentInFragment(
                    R.id.mainLayoutFragment, settings,
                    "MAIN_TAB", "FIRST_TAB"
                )


            }
        }

    }
    override fun onBackTriggered() {
        home()?.resetAndExit();
    }
}