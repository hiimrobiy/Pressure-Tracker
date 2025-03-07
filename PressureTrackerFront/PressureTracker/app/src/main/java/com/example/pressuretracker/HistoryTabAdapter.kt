package com.example.pressuretracker

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
/**
 * ++
 */
class HistoryTabAdapter (val fm : FragmentManager, val context: Context, val totalTabs:Int) : FragmentPagerAdapter(fm){

    override fun getCount(): Int {
        return totalTabs;
    }
    override fun getItem(position: Int): Fragment {
        return  when(position){
            0-> BloodPressureFragmentTab()
            1-> SugarLevelFragmentTab()
            else ->BloodPressureFragmentTab()
        }
    }
}