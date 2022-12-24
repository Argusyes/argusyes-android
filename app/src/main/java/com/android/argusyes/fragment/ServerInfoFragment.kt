package com.android.argusyes.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.argusyes.R
import com.android.argusyes.dao.ServerDao
import com.android.argusyes.dao.entity.Server
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class ServerInfoFragment : Fragment() {

    private var servers : MutableList<Server> = LinkedList<Server>()
    private var serverDao: ServerDao? = null

    private var titleLayout : LinearLayout? = null
    private var titleTitleButton : ImageButton? = null

    private var searchInput : TextInputEditText? = null
    private var searchCancelButton : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverDao = context?.let { ServerDao.getInstance(it)}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_server_info, container, false)
        titleLayout = view.findViewById(R.id.server_info_title_layout)
        titleTitleButton = view.findViewById<ImageButton>(R.id.server_info_title_button)
        titleTitleButton?.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_serverInfoFragment_to_serverAddFragment))

        searchInput = view.findViewById(R.id.server_info_search_input)
        searchCancelButton = view.findViewById(R.id.server_info_search_cancel_button)

        searchCancelButton?.apply {
            setOnClickListener {
                searchInput?.clearFocus()
            }
        }

        searchInput?.apply {
            onFocusChangeListener = View.OnFocusChangeListener { view, isFocus ->
                if (isFocus) {
                    searchCancelButton?.visibility = View.VISIBLE
                    titleLayout?.visibility = View.GONE
                } else {
                    searchCancelButton?.visibility = View.GONE
                    titleLayout?.visibility = View.VISIBLE
                    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        servers.clear()
        serverDao?.let { servers.addAll(it.list()) }
    }
}