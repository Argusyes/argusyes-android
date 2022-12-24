package com.android.argusyes.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.argusyes.R
import com.android.argusyes.dao.ServerDao
import com.android.argusyes.dao.entity.Server
import com.google.android.material.textfield.TextInputEditText


class ServerAddFragment : Fragment() {

    private var saveButton : Button? = null
    private var cancelButton : Button? = null

    private var nameTextInput : TextInputEditText? = null
    private var hostTextInput : TextInputEditText? = null
    private var portTextInput : TextInputEditText? = null
    private var usernameTextInput : TextInputEditText? = null
    private var passwordTextInput : TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val serverDao = activity?.let { ServerDao.getInstance(it) }

        val view = inflater.inflate(R.layout.fragment_server_add, container, false)
        saveButton = view.findViewById(R.id.server_add_title_save_button)
        cancelButton = view.findViewById(R.id.server_add_title_cancel_button)
        nameTextInput = view.findViewById(R.id.server_add_name_input)
        hostTextInput = view.findViewById(R.id.server_add_host_input)
        portTextInput = view.findViewById(R.id.server_add_port_input)
        usernameTextInput = view.findViewById(R.id.server_add_username_input)
        passwordTextInput = view.findViewById(R.id.server_add_password_input)

        val name = nameTextInput?.text.toString()
        val host = hostTextInput?.text.toString()
        val port = portTextInput?.text.toString().toInt()
        val username = usernameTextInput?.text.toString()
        val password = passwordTextInput?.text.toString()
        val server = Server(id = null, name, host, port, username, password)
        saveButton?.setOnClickListener {
            serverDao?.add(server)
            it.findNavController().popBackStack()
        }


        cancelButton?.setOnClickListener { it.findNavController().popBackStack() }
        return view
    }

}