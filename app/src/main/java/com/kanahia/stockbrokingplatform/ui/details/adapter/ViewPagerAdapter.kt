package com.kanahia.stockbrokingplatform.ui.details.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kanahia.stockbrokingplatform.ui.details.fragments.DetailsFragment
import com.kanahia.stockbrokingplatform.ui.details.fragments.NewsFragment
import com.kanahia.stockbrokingplatform.ui.details.fragments.SummaryFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SummaryFragment()
            1 -> DetailsFragment()
            else -> SummaryFragment()
        }
    }
}