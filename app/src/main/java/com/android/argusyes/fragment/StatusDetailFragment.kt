package com.android.argusyes.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.argusyes.R
import com.android.argusyes.ssh.SSHManager

class StatusDetailFragment : Fragment() {

    private var serverId: String? = null

    private var sshManager: SSHManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sshManager = context?.let { SSHManager.getInstance(it) }
        arguments?.let {
            serverId = it.getString(SERVER_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status_detail, container, false)
        return view
    }

}