package com.android.argusyes.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.argusyes.R
import com.android.argusyes.dao.entity.Server
import com.android.argusyes.ssh.SSHManager
import com.android.argusyes.ui.CircleProgress
import com.android.argusyes.utils.FlipUtils
import com.google.android.material.textfield.TextInputEditText

class StatusInfoFragment : Fragment() {

    private var sshManager: SSHManager? = null

    private var titleText : TextView? = null
    private var searchInput : TextInputEditText? = null
    private var searchCancelButton : Button? = null
    private var listView : ListView? = null
    private var adapter : StatusBaseAdapter? = null

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
                    adapter?.run { listView?.adapter = this  }
                }
            }

            setOnEditorActionListener { textView, _, _ ->
                val key = textView?.text.toString()
                val servers = sshManager?.getServers()
                val res : List<Server> = servers?.filter { it.name.contains(key) } as List<Server>
                if (key.isNotEmpty()) {
                    context?.run { listView?.adapter = StatusBaseAdapter(this, res) }
                } else {
                    adapter?.run { listView?.adapter = this  }
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

        context?.run {
            adapter = sshManager?.getServers()?.let { StatusBaseAdapter(this, it) }
            adapter?.let { listView?.adapter = adapter }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
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

            holder.cpuLoadFlipOutLayout = view.findViewById(R.id.status_info_cpu_load_flip_layout)
            holder.cpuFlipLayout = view.findViewById(R.id.status_info_cpu_flip_layout)
            holder.loadFlipLayout = view.findViewById(R.id.status_info_load_flip_layout)

            holder.cpuBar = view.findViewById(R.id.status_info_cpu_bar)
            holder.loadBar1 = view.findViewById(R.id.status_info_load_bar_1)
            holder.loadBar2 = view.findViewById(R.id.status_info_load_bar_2)
            holder.loadBar3 = view.findViewById(R.id.status_info_load_bar_3)

            view.tag = holder
        } else {
            holder = view.tag as StatusViewHolder
        }
        val server = servers[index]
        holder.nameTextView?.text = server.name

        holder.cpuLoadFlipOutLayout?.setOnClickListener {
            FlipUtils.flipAnimation(holder.cpuFlipLayout, holder.loadFlipLayout)
        }

        holder.loadBar1?.progress = 50
        holder.loadBar2?.progress = 50
        holder.loadBar3?.progress = 50
        holder.cpuBar?.setProgress(50F)
        holder.cpuBar?.setProgress(50F)

        holder.loadBar1?.secondaryProgress = 80
        holder.loadBar2?.secondaryProgress = 80
        holder.loadBar3?.secondaryProgress = 80

        assert(view != null)
        return view!!
    }

}

class StatusViewHolder {
    var nameTextView: TextView? = null
    var cpuFlipLayout : LinearLayout? = null
    var loadFlipLayout : LinearLayout? = null
    var cpuLoadFlipOutLayout : LinearLayout? = null

    var cpuBar : CircleProgress? = null
    var loadBar1 : ProgressBar? = null
    var loadBar2 : ProgressBar? = null
    var loadBar3 : ProgressBar? = null
}