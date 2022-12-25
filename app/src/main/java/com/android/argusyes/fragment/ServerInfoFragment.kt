package com.android.argusyes.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.argusyes.R
import com.android.argusyes.dao.entity.Server
import com.android.argusyes.ssh.SSHManager
import com.google.android.material.textfield.TextInputEditText


class ServerInfoFragment : Fragment() {

    private var sshManager: SSHManager? = null

    private var titleLayout : LinearLayout? = null
    private var titleTitleButton : ImageButton? = null
    private var searchInput : TextInputEditText? = null
    private var searchCancelButton : Button? = null
    private var listView : ListView ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        sshManager = context?.let { SSHManager.getInstance(it)}
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_server_info, container, false)
        titleLayout = view.findViewById(R.id.server_info_title_layout)
        titleTitleButton = view.findViewById<ImageButton>(R.id.server_info_title_button)
        searchInput = view.findViewById(R.id.server_info_search_input)
        searchCancelButton = view.findViewById(R.id.server_info_search_cancel_button)
        listView = view.findViewById(R.id.server_info_list_view)

        titleTitleButton?.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_serverInfoFragment_to_serverAddFragment))

        searchInput?.apply {
            onFocusChangeListener = View.OnFocusChangeListener { view, isFocus ->
                if (isFocus) {
                    searchCancelButton?.visibility = View.VISIBLE
                    titleLayout?.visibility = View.GONE
                } else {
                    setText("")
                    searchCancelButton?.visibility = View.GONE
                    titleLayout?.visibility = View.VISIBLE
                    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    context?.run { listView?.adapter = sshManager?.getServers()?.let { ServerBaseAdapter(this, it) } }
                }
            }

            setOnEditorActionListener { textView, _, _ ->
                val key = textView?.text.toString()
                val servers = sshManager?.getServers()
                val res : List<Server> = servers?.filter { it.name.contains(key) } as List<Server>
                if (key.isNotEmpty()) {
                    context?.run { listView?.adapter = ServerBaseAdapter(this, res) }
                } else {
                    context?.run { listView?.adapter = sshManager?.getServers()?.let { ServerBaseAdapter(this, it) } }
                }
                return@setOnEditorActionListener false
            }
        }

        searchCancelButton?.apply {
            setOnClickListener {
                searchInput?.apply {
                    clearFocus()
                }
            }
        }

        context?.run { listView?.adapter = sshManager?.getServers()?.let { ServerBaseAdapter(this, it) } }
        return view
    }

    override fun onResume() {
        super.onResume()
    }
}

class ServerBaseAdapter (context: Context, private val servers: List<Server>) : BaseAdapter () {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private val sshManager = SSHManager.getInstance(context)

    override fun getCount(): Int {
        return servers.size
    }

    override fun getItem(index: Int): Any {
        return servers[index]
    }

    override fun getItemId(index: Int): Long {
       return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ServerViewHolder?
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_server, parent, false)
            holder = ServerViewHolder()
            holder.nameTextView = view.findViewById(R.id.item_name_text_view)
            holder.identifyTextView = view.findViewById(R.id.item_identify_text_view)
            holder.changeImageButton = view.findViewById(R.id.item_change_button)
            holder.deleteImageButton = view.findViewById(R.id.item_delete_button)
            view.tag = holder
        } else {
            holder = view.tag as ServerViewHolder
        }
        val server = servers[index]
        holder.nameTextView?.text = server.name
        val identify = "${server.userName}@${server.host}:${server.port}"
        holder.identifyTextView?.text = identify

        holder.deleteImageButton?.setOnClickListener {
            sshManager.removeServerById(server.id)
            notifyDataSetChanged()
        }
        holder.changeImageButton?.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_serverInfoFragment_to_serverAddFragment,
            Bundle().apply {
                putString(SERVER_ID, server.id)
            }
        ))

        assert(view != null)
        return view!!
    }
}

class ServerViewHolder {
    var nameTextView: TextView? = null
    var identifyTextView: TextView? = null
    var changeImageButton: ImageButton? = null
    var deleteImageButton: ImageButton? = null
}