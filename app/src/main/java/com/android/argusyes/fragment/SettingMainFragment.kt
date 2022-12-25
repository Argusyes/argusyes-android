package com.android.argusyes.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.Navigation
import com.android.argusyes.R


class SettingMainFragment : Fragment() {

    private var aboutLayout : LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setting_main, container, false)
        aboutLayout = view.findViewById(R.id.setting_main_about_layout)

        aboutLayout?.setOnClickListener( Navigation.createNavigateOnClickListener(R.id.action_settingMainFragment_to_settingAboutFragment))
        return view
    }

}