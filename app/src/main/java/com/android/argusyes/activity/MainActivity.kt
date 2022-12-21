package com.android.argusyes.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.argusyes.R
import com.android.argusyes.fragment.ServerFragment
import com.android.argusyes.fragment.SettingFragment
import com.android.argusyes.fragment.StatusFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {

        val viewPager2: ViewPager2 = findViewById(R.id.mainViewPage)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.navigationBottomView)

        // 设置FragmentStateAdapter
        viewPager2.adapter = MyFragmentStateAdapter(this)

        // 当ViewPager切换页面时，改变底部导航栏的状态
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        // 当ViewPager切换页面时，改变ViewPager的显示
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_server -> {
                    viewPager2.setCurrentItem(0, true)
                }
                R.id.navigation_status -> {
                    viewPager2.setCurrentItem(1, true)
                }
                R.id.navigation_setting -> {
                    viewPager2.setCurrentItem(2, true)
                }
            }
            true
        }
    }
}

class MyFragmentStateAdapter(activity:FragmentActivity):FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 3
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ServerFragment()
            1 -> StatusFragment()
            else -> SettingFragment()
        }
    }
}
