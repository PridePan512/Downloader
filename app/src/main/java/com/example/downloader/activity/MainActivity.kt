package com.example.downloader.activity

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.downloader.R
import com.example.downloader.fragment.DownloadFragment
import com.example.downloader.fragment.HistoryFragment
import com.example.downloader.utils.AndroidUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
    }

    private fun initView() {
        val viewPager2 = findViewById<ViewPager2>(R.id.v_viewpager2)
        val tabLayout = findViewById<TabLayout>(R.id.v_tablayout)
        viewPager2.offscreenPageLimit = 2

        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    DownloadFragment()
                } else {
                    HistoryFragment()
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }

        TabLayoutMediator(
            tabLayout,
            viewPager2,
            object : TabLayoutMediator.TabConfigurationStrategy {
                override fun onConfigureTab(
                    tab: TabLayout.Tab,
                    position: Int
                ) {
                    tab.setText(
                        if (position == 0) {
                            "Download"
                        } else {
                            "History"
                        }
                    )
                }
            }).attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                setTextStyle(tab, StyleSpan(Typeface.BOLD))

                if (tab?.position != 0) {
                    AndroidUtil.hideKeyboard(this@MainActivity, tabLayout)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                setTextStyle(tab, StyleSpan(Typeface.NORMAL))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    private fun setTextStyle(tab: TabLayout.Tab?, style: StyleSpan) {
        tab?.let {
            val spStr = SpannableString(it.text)

            val spans = spStr.getSpans(0, spStr.length, StyleSpan::class.java)
            for (span in spans) {
                spStr.removeSpan(span)
            }

            spStr.setSpan(style, 0, it.text!!.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            it.text = spStr
        }
    }
}