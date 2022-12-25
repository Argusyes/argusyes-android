package com.android.argusyes.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.argusyes.R
import com.android.argusyes.dao.entity.Server
import com.android.argusyes.ssh.SSHManager
import com.google.android.material.textfield.TextInputEditText

class StatusInfoFragment : Fragment() {

    private var sshManager: SSHManager? = null

    private var titleText : TextView? = null
    private var searchInput : TextInputEditText? = null
    private var searchCancelButton : Button? = null
    private var listView : ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        sshManager = context?.let { SSHManager.getInstance(it)}
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status_info, container, false)
        titleText = view.findViewById(R.id.status_info_title)
        searchInput = view.findViewById(R.id.status_info_search_input)
        searchCancelButton = view.findViewById(R.id.status_info_search_cancel_button)
        listView = view.findViewById(R.id.status_info_list_view)

        searchInput?.apply {
            onFocusChangeListener = View.OnFocusChangeListener { view, isFocus ->
                if (isFocus) {
                    searchCancelButton?.visibility = View.VISIBLE
                    titleText?.visibility = View.GONE
                } else {
                    setText("")
                    searchCancelButton?.visibility = View.GONE
                    titleText?.visibility = View.VISIBLE
                    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    context?.run { listView?.adapter = sshManager?.getServers()?.let { StatusBaseAdapter(this, it) } }
                }
            }

            setOnEditorActionListener { textView, _, _ ->
                val key = textView?.text.toString()
                val servers = sshManager?.getServers()
                val res : List<Server> = servers?.filter { it.name.contains(key) } as List<Server>
                if (key.isNotEmpty()) {
                    context?.run { listView?.adapter = StatusBaseAdapter(this, res) }
                } else {
                    context?.run { listView?.adapter = sshManager?.getServers()?.let { StatusBaseAdapter(this, it) } }
                }
                false
            }
        }

        searchCancelButton?.apply {
            setOnClickListener {
                searchInput?.apply {
                    clearFocus()
                }
            }
        }

        context?.run { listView?.adapter = sshManager?.getServers()?.let { StatusBaseAdapter(this, it) } }
        return view
    }
}

class StatusBaseAdapter (context: Context, private val servers: List<Server>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

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
        val holder: StatusViewHolder?
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_status, parent, false)
            holder = StatusViewHolder()
            holder.nameTextView = view.findViewById(R.id.item_name_text_view)
            view.tag = holder
        } else {
            holder = view.tag as StatusViewHolder
        }
        val server = servers[index]
        holder.nameTextView?.text = server.name

        assert(view != null)
        return view!!
    }
}

class StatusViewHolder {
    var nameTextView: TextView? = null
}