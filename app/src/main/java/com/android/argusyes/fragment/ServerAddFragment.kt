package com.android.argusyes.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.argusyes.R
import com.android.argusyes.dao.entity.Server
import com.android.argusyes.ssh.SSHManager
import com.android.argusyes.utils.AlertUtils
import com.google.android.material.textfield.TextInputEditText

const val SERVER_ID = "SERVER_ID"

class ServerAddFragment : Fragment() {

    private var serverId: String? = null

    private var sshManager: SSHManager? = null

    private var saveButton : Button? = null
    private var cancelButton : Button? = null
    private var nameTextInput : TextInputEditText? = null
    private var hostTextInput : TextInputEditText? = null
    private var portTextInput : TextInputEditText? = null
    private var usernameTextInput : TextInputEditText? = null
    private var passwordTextInput : TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sshManager = context?.let { SSHManager.getInstance(it) }
        arguments?.let {
            serverId = it.getString(SERVER_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_server_add, container, false)
        saveButton = view.findViewById(R.id.server_add_title_save_button)
        cancelButton = view.findViewById(R.id.server_add_title_cancel_button)
        nameTextInput = view.findViewById(R.id.server_add_name_input)
        hostTextInput = view.findViewById(R.id.server_add_host_input)
        portTextInput = view.findViewById(R.id.server_add_port_input)
        usernameTextInput = view.findViewById(R.id.server_add_username_input)
        passwordTextInput = view.findViewById(R.id.server_add_password_input)

        saveButton?.setOnClickListener {
            val name = nameTextInput?.text.toString()
            if (name.isEmpty()) {
                AlertUtils.alter("名称不能为空", context)
                return@setOnClickListener
            }
            val host = hostTextInput?.text.toString()
            if (host.isEmpty()) {
                AlertUtils.alter("主机不能为空", context)
                return@setOnClickListener
            }
            val portString = portTextInput?.text.toString()
            if (portString.isEmpty()) {
                AlertUtils.alter("端口不能为空", context)
                return@setOnClickListener
            }
            val port = portString.toInt()
            val username = usernameTextInput?.text.toString()
            if (host.isEmpty()) {
                AlertUtils.alter("用户不能为空", context)
                return@setOnClickListener
            }
            val password = passwordTextInput?.text.toString()
            if (serverId == null) {
                sshManager?.addServer(Server(name = name, host = host, port = port, userName = username, password = password))
            } else {
                sshManager?.updateServerById(Server(id = serverId!!, name = name, host = host, port = port, userName = username, password = password))
            }

            it.findNavController().popBackStack()
        }

        cancelButton?.setOnClickListener { it.findNavController().popBackStack() }

        val ssh = serverId?.let { sshManager?.getServerById(it) }
        ssh?.run {
            nameTextInput?.setText(data.name)
            hostTextInput?.setText(data.host)
            portTextInput?.setText(data.port.toString())
            usernameTextInput?.setText(data.userName)
            passwordTextInput?.setText(data.password)
        }
        return view
    }
}