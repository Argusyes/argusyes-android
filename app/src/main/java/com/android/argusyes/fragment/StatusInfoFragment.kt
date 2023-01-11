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
import com.android.argusyes.ssh.ConnectStatus
import com.android.argusyes.ssh.SSH
import com.android.argusyes.ssh.SSHManager
import com.android.argusyes.ui.CircleProgress
import com.android.argusyes.ui.ThreeCircleProgress
import com.android.argusyes.utils.FlipUtils
import com.android.argusyes.utils.formatPrint
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import pl.droidsonroids.gif.GifImageView

class StatusInfoFragment : Fragment() {

    private var sshManager: SSHManager? = null

    @OptIn(DelicateCoroutinesApi::class)
    private var job: Job = GlobalScope.launch(Dispatchers.Main) {
        while (isActive) {
            delay(2000)
            adapter?.notifyDataSetChanged()
        }
    }

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
                val sshs = sshManager?.getSSH()
                val res : List<SSH> = sshs?.filter { it.data.name.contains(key) } as List<SSH>
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
            adapter = sshManager?.getSSH()?.let { StatusBaseAdapter(this, it) }
            adapter?.let { listView?.adapter = adapter }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
        if (!job.isActive) {
            job.start()
        }
    }
}

class StatusBaseAdapter (context: Context, private val sshs: List<SSH>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return sshs.size
    }

    override fun getItem(index: Int): Any {
        return sshs[index]
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

            holder.itemLayout = view.findViewById(R.id.status_item_layout)

            holder.nameTextView = view.findViewById(R.id.status_item_name_text_view)

            holder.tempLayout = view.findViewById(R.id.status_item_temp_layout)
            holder.connectFailTextView = view.findViewById(R.id.status_item_connect_fail_text_view)
            holder.connectingGif = view.findViewById(R.id.status_item_loading_gif)

            holder.tempTextView = view.findViewById(R.id.status_item_temp_text_view)

            holder.cpuLoadFlipOutLayout = view.findViewById(R.id.status_info_cpu_load_flip_layout)
            holder.cpuFlipLayout = view.findViewById(R.id.status_info_cpu_flip_layout)
            holder.loadFlipLayout = view.findViewById(R.id.status_info_load_flip_layout)

            holder.cpuBar = view.findViewById(R.id.status_info_cpu_bar)
            holder.loadBar = view.findViewById(R.id.status_info_load_bar)

            holder.memSwapFlipOutLayout = view.findViewById(R.id.status_info_mem_swap_flip_layout)
            holder.memFlipLayout = view.findViewById(R.id.status_info_mem_flip_layout)
            holder.swapFlipLayout = view.findViewById(R.id.status_info_swap_flip_layout)

            holder.memBar = view.findViewById(R.id.status_info_mem_bar)
            holder.swapBar = view.findViewById(R.id.status_info_swap_bar)

            holder.netFlipOutLayout = view.findViewById(R.id.status_info_net_flip_layout)
            holder.netSpeedFlipLayout = view.findViewById(R.id.status_info_net_speed_flip_layout)
            holder.netTotalFlipLayout = view.findViewById(R.id.status_info_net_total_flip_layout)

            holder.netUpSpeedTextView = view.findViewById(R.id.status_info_net_up_speed_text_view)
            holder.netUpSpeedUnitTextView = view.findViewById(R.id.status_info_net_up_speed_unit_text_view)
            holder.netDownSpeedTextView = view.findViewById(R.id.status_info_net_down_speed_text_view)
            holder.netDownSpeedUnitTextView = view.findViewById(R.id.status_info_net_down_speed_unit_text_view)

            holder.netUpTextView = view.findViewById(R.id.status_info_net_up_text_view)
            holder.netUpUnitTextView = view.findViewById(R.id.status_info_net_up_unit_text_view)
            holder.netDownTextView = view.findViewById(R.id.status_info_net_down_text_view)
            holder.netDownUnitTextView = view.findViewById(R.id.status_info_net_down_unit_text_view)

            holder.storeFlipOutLayout = view.findViewById(R.id.status_info_store_flip_layout)
            holder.storeSpeedFlipLayout = view.findViewById(R.id.status_info_store_speed_flip_layout)
            holder.storeTotalFlipLayout = view.findViewById(R.id.status_info_store_total_flip_layout)

            holder.storeWriteSpeedTextView = view.findViewById(R.id.status_info_store_write_speed_text_view)
            holder.storeWriteSpeedUnitTextView = view.findViewById(R.id.status_info_store_write_speed_unit_text_view)
            holder.storeReadSpeedTextView = view.findViewById(R.id.status_info_store_read_speed_text_view)
            holder.storeReadSpeedUnitTextView = view.findViewById(R.id.status_info_store_read_speed_unit_text_view)

            holder.storeWriteTextView = view.findViewById(R.id.status_info_store_write_text_view)
            holder.storeWriteUnitTextView = view.findViewById(R.id.status_info_store_write_unit_text_view)
            holder.storeReadTextView = view.findViewById(R.id.status_info_store_read_text_view)
            holder.storeReadUnitTextView = view.findViewById(R.id.status_info_store_read_unit_text_view)

            view.tag = holder
        } else {
            holder = view.tag as StatusViewHolder
        }

        val ssh = sshs[index]

        holder.itemLayout?.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_statusInfoFragment_to_statusDetailFragment,
                Bundle().apply {
                    putString(SERVER_ID, ssh.data.id)
                }
            )
        )

        holder.nameTextView?.text = ssh.data.name

        holder.connectingGif?.visibility = View.GONE
        holder.tempLayout?.visibility = View.GONE
        holder.connectFailTextView?.visibility = View.GONE

        when (ssh.monitor.connectStatus) {
            ConnectStatus.INIT -> holder.connectingGif?.visibility = View.VISIBLE
            ConnectStatus.SUCCESS -> holder.tempLayout?.visibility = View.VISIBLE
            ConnectStatus.FAIL -> holder.connectFailTextView?.visibility = View.VISIBLE
        }

        holder.tempTextView?.text = ssh.monitor.monitorInfo.temp.map.values.takeIf { it.isNotEmpty() }?.max()?.toString() ?: "0"

        holder.cpuLoadFlipOutLayout?.setOnClickListener {
            FlipUtils.flipAnimation(holder.cpuFlipLayout, holder.loadFlipLayout)
        }

        holder.memSwapFlipOutLayout?.setOnClickListener {
            FlipUtils.flipAnimation(holder.memFlipLayout, holder.swapFlipLayout)
        }

        holder.netFlipOutLayout?.setOnClickListener {
            FlipUtils.flipAnimation(holder.netSpeedFlipLayout, holder.netTotalFlipLayout)
        }

        holder.storeFlipOutLayout?.setOnClickListener {
            FlipUtils.flipAnimation(holder.storeSpeedFlipLayout, holder.storeTotalFlipLayout)
        }

        holder.cpuBar?.setProgress(ssh.monitor.monitorInfo.cpus.total.utilization)

        holder.memBar?.setProgress(ssh.monitor.monitorInfo.mem.usedOccupy)
        holder.memBar?.setProgressSecond(ssh.monitor.monitorInfo.mem.cacheOccupy)

        holder.swapBar?.setProgress(ssh.monitor.monitorInfo.mem.usedSwapOccupy)
        holder.swapBar?.setProgressSecond(ssh.monitor.monitorInfo.mem.cachedSwapOccupy)

        holder.loadBar?.setProgress(ssh.monitor.monitorInfo.loadavg.oneOccupy)
        holder.loadBar?.setProgressSecond(ssh.monitor.monitorInfo.loadavg.fiveOccupy)
        holder.loadBar?.setProgressThree(ssh.monitor.monitorInfo.loadavg.fifteenOccupy)

        holder.netUpSpeedTextView?.text = ssh.monitor.monitorInfo.netDevs.total.upSpeed.formatPrint()
        holder.netUpSpeedUnitTextView?.text = ssh.monitor.monitorInfo.netDevs.total.upSpeedUnit
        holder.netDownSpeedTextView?.text = ssh.monitor.monitorInfo.netDevs.total.downSpeed.formatPrint()
        holder.netDownSpeedUnitTextView?.text = ssh.monitor.monitorInfo.netDevs.total.downSpeedUnit

        holder.netUpTextView?.text = ssh.monitor.monitorInfo.netDevs.total.upBytesH.formatPrint()
        holder.netUpUnitTextView?.text = ssh.monitor.monitorInfo.netDevs.total.upBytesHUnit
        holder.netDownTextView?.text = ssh.monitor.monitorInfo.netDevs.total.downBytesH.formatPrint()
        holder.netDownUnitTextView?.text = ssh.monitor.monitorInfo.netDevs.total.downBytesHUnit

        holder.storeWriteSpeedTextView?.text = ssh.monitor.monitorInfo.disks.total.writeSpeed.formatPrint()
        holder.storeWriteSpeedUnitTextView?.text = ssh.monitor.monitorInfo.disks.total.writeSpeedUnit
        holder.storeReadSpeedTextView?.text = ssh.monitor.monitorInfo.disks.total.readSpeed.formatPrint()
        holder.storeReadSpeedUnitTextView?.text = ssh.monitor.monitorInfo.disks.total.readSpeedUnit

        holder.storeWriteTextView?.text = ssh.monitor.monitorInfo.disks.total.write.formatPrint()
        holder.storeWriteUnitTextView?.text = ssh.monitor.monitorInfo.disks.total.writeUnit
        holder.storeReadTextView?.text = ssh.monitor.monitorInfo.disks.total.read.formatPrint()
        holder.storeReadUnitTextView?.text = ssh.monitor.monitorInfo.disks.total.readUnit

        assert(view != null)
        return view!!
    }

}


class StatusViewHolder {

    var itemLayout : LinearLayout? = null

    var nameTextView: TextView? = null

    var tempLayout: LinearLayout? = null
    var connectFailTextView: TextView? = null
    var connectingGif: GifImageView? = null

    var tempTextView : TextView? = null

    var cpuFlipLayout : LinearLayout? = null
    var loadFlipLayout : LinearLayout? = null
    var cpuLoadFlipOutLayout : LinearLayout? = null

    var cpuBar : CircleProgress? = null
    var loadBar : ThreeCircleProgress? = null

    var memFlipLayout : LinearLayout? = null
    var swapFlipLayout : LinearLayout? = null
    var memSwapFlipOutLayout : LinearLayout? = null

    var memBar : CircleProgress? = null
    var swapBar : CircleProgress? = null

    var netSpeedFlipLayout : LinearLayout? = null
    var netTotalFlipLayout : LinearLayout? = null
    var netFlipOutLayout : LinearLayout? = null

    var netUpSpeedTextView : TextView? = null
    var netUpSpeedUnitTextView : TextView? = null
    var netDownSpeedTextView : TextView? = null
    var netDownSpeedUnitTextView : TextView? = null

    var netUpTextView : TextView? = null
    var netUpUnitTextView : TextView? = null
    var netDownTextView : TextView? = null
    var netDownUnitTextView : TextView? = null

    var storeSpeedFlipLayout : LinearLayout? = null
    var storeTotalFlipLayout : LinearLayout? = null
    var storeFlipOutLayout : LinearLayout? = null

    var storeWriteSpeedTextView : TextView? = null
    var storeWriteSpeedUnitTextView : TextView? = null
    var storeReadSpeedTextView : TextView? = null
    var storeReadSpeedUnitTextView : TextView? = null

    var storeWriteTextView : TextView? = null
    var storeWriteUnitTextView : TextView? = null
    var storeReadTextView : TextView? = null
    var storeReadUnitTextView : TextView? = null
}