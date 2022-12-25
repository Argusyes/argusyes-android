package com.android.argusyes.fragment

import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.argusyes.R
import com.android.argusyes.ui.NoUnderlineSpan


class SettingAboutFragment : Fragment() {

    private var backButton : Button? = null
    private var githubTextView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setting_about, container, false)
        backButton = view.findViewById(R.id.setting_about_title_button)
        githubTextView = view.findViewById(R.id.setting_about_github_text_view)

        val mNoUnderlineSpan = NoUnderlineSpan()

        backButton?.setOnClickListener{
            it.findNavController().popBackStack()
        }

        githubTextView?.run {
            if (text is Spannable) {
                (text as Spannable).setSpan(mNoUnderlineSpan, 0, text.length, Spanned.SPAN_MARK_MARK)
            }
        }

        return view
    }

}