package com.android.argusyes.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.argusyes.R
import com.android.argusyes.ssh.*
import com.android.argusyes.ui.CircleProgress
import com.android.argusyes.ui.ListViewForScrollView
import com.android.argusyes.ui.ThreeCircleProgress
import com.android.argusyes.utils.formatPrint
import kotlinx.coroutines.*
import java.util.*


class StatusDetailFragment : Fragment() {

    private var job: Job? = null

    private var sshManager: SSHManager? = null

    private var serverId: String? = null
    private var ssh: SSH? = null

    private var titleTextView: TextView? = null
    private var titleButton: Button? = null

    private var cpuTotalTextView: TextView? = null
    private var cpuSystemTextView: TextView? = null
    private var cpuUserTextView: TextView? = null
    private var cpuIOTextView: TextView? = null
    private var cpuNiceTextView: TextView? = null

    private var cpuCoreListView: ListViewForScrollView? = null

    private var cpuNumTextView: TextView? = null
    private var cpuFreeTextView: TextView? = null

    private var uptimeTextView: TextView? = null
    private var uptimeUnitTextView: TextView? = null

    private var loadBar: ThreeCircleProgress? = null

    private var memFreeTextView: TextView? = null
    private var memFreeUnitUnitTextView: TextView? = null
    private var memUsedTextView: TextView? = null
    private var memUsedUnitUnitTextView: TextView? = null
    private var memCacheTextView: TextView? = null
    private var memCacheUnitUnitTextView: TextView? = null
    private var memBar: CircleProgress? = null

    private var netUpSpeedTextView: TextView? = null
    private var netUpSpeedUnitTextView: TextView? = null
    private var netDownSpeedTextView: TextView? = null
    private var netDownSpeedUnitTextView: TextView? = null
    private var netUpTextView: TextView? = null
    private var netUpUnitTextView: TextView? = null
    private var netDownTextView: TextView? = null
    private var netDownUnitTextView: TextView? = null
    private var netBar: CircleProgress? = null

    private var netReTranTextView: TextView? = null
    private var netActiveTextView: TextView? = null
    private var netPassiveTextView: TextView? = null
    private var netFailTextView: TextView? = null


    private var netDevListView: ListViewForScrollView? = null
    private var storeListView: ListViewForScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sshManager = context?.let { SSHManager.getInstance(it) }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status_detail, container, false)

        arguments?.let {
            serverId = it.getString(SERVER_ID)
            ssh = serverId?.let { id -> sshManager?.getServerById(id) }
        }

        titleTextView = view.findViewById(R.id.status_detail_title)
        titleButton = view.findViewById(R.id.status_detail_title_button)
        titleButton?.setOnClickListener { it.findNavController().popBackStack() }

        cpuTotalTextView = view.findViewById(R.id.status_detail_cpu_total_text_view)
        cpuSystemTextView = view.findViewById(R.id.status_detail_cpu_system_text_view)
        cpuUserTextView = view.findViewById(R.id.status_detail_cpu_user_text_view)
        cpuIOTextView = view.findViewById(R.id.status_detail_cpu_io_text_view)
        cpuNiceTextView = view.findViewById(R.id.status_detail_cpu_nice_text_view)

        cpuCoreListView = view.findViewById(R.id.status_detail_cpu_core_list_view)

        cpuNumTextView = view.findViewById(R.id.status_detail_cpu_num_text_view)
        cpuFreeTextView = view.findViewById(R.id.status_detail_cpu_free_text_view)

        uptimeTextView = view.findViewById(R.id.status_detail_uptime_text_view)
        uptimeUnitTextView= view.findViewById(R.id.status_detail_uptime_unit_text_view)

        netDevListView = view.findViewById(R.id.status_detail_net_dev_list_view)
        storeListView = view.findViewById(R.id.status_detail_store_list_view)

        loadBar = view.findViewById(R.id.status_detail_load_bar)

        memFreeTextView = view.findViewById(R.id.status_detail_mem_free_text_view)
        memFreeUnitUnitTextView = view.findViewById(R.id.status_detail_mem_free_unit_text_view)
        memUsedTextView = view.findViewById(R.id.status_detail_mem_used_text_view)
        memUsedUnitUnitTextView = view.findViewById(R.id.status_detail_mem_used_unit_text_view)
        memCacheTextView = view.findViewById(R.id.status_detail_mem_cache_text_view)
        memCacheUnitUnitTextView = view.findViewById(R.id.status_detail_mem_cache_unit_text_view)
        memBar = view.findViewById(R.id.status_detail_mem_bar)

        val netDevHeaderView = inflater.inflate(R.layout.item_status_net_header, container, false)

        netUpSpeedTextView = netDevHeaderView.findViewById(R.id.net_head_up_speed_text_view)
        netUpSpeedUnitTextView = netDevHeaderView.findViewById(R.id.net_head_up_speed_unit_text_view)
        netDownSpeedTextView = netDevHeaderView.findViewById(R.id.net_head_down_speed_text_view)
        netDownSpeedUnitTextView = netDevHeaderView.findViewById(R.id.net_head_down_speed_unit_text_view)
        netUpTextView = netDevHeaderView.findViewById(R.id.net_head_up_text_view)
        netUpUnitTextView = netDevHeaderView.findViewById(R.id.net_head_up_unit_text_view)
        netDownTextView = netDevHeaderView.findViewById(R.id.net_head_down_text_view)
        netDownUnitTextView = netDevHeaderView.findViewById(R.id.net_head_down_unit_text_view)
        netBar = netDevHeaderView.findViewById(R.id.net_head_bar)

        netReTranTextView = netDevHeaderView.findViewById(R.id.net_head_re_tran_text_view)
        netActiveTextView = netDevHeaderView.findViewById(R.id.net_head_active_text_view)
        netPassiveTextView = netDevHeaderView.findViewById(R.id.net_head_passive_text_view)
        netFailTextView = netDevHeaderView.findViewById(R.id.net_head_fail_text_view)

        netDevListView?.addHeaderView(netDevHeaderView)

        updateView()
        if (job == null) {
            job = GlobalScope.launch(Dispatchers.Main) {
                while (isActive) {
                    delay(2000)
                    updateView()
                }
            }
        }

        job?.let {
            if (!it.isActive) {
                it.start()
            }
        }

        return view
    }

    private fun updateView () {
        ssh?.let {
            titleTextView?.text = it.data.name
            cpuTotalTextView?.text = it.monitor.monitorInfo.cpus.total.utilization.toInt().toString()

            cpuSystemTextView?.text = it.monitor.monitorInfo.cpus.total.system.toInt().toString()
            cpuUserTextView?.text = it.monitor.monitorInfo.cpus.total.user.toInt().toString()
            cpuIOTextView?.text = it.monitor.monitorInfo.cpus.total.ioWait.toInt().toString()
            cpuNiceTextView?.text = it.monitor.monitorInfo.cpus.total.nice.toInt().toString()

            cpuCoreListView?.adapter = context?.let { c -> StatusCpuCoreBaseAdapter(c, it.monitor.monitorInfo.cpus.cpus) }

            cpuNumTextView?.text = it.monitor.monitorInfo.cpus.cpus.size.toString()
            cpuFreeTextView?.text = it.monitor.monitorInfo.cpus.total.free.toInt().toString()

            val uptime = UnitData(0f, "S")
            if (it.monitor.monitorInfo.uptime.upDay != 0) {
                uptime.data = it.monitor.monitorInfo.uptime.upDay.toFloat()
                uptime.unit = "D"
            } else if (it.monitor.monitorInfo.uptime.upHour != 0) {
                uptime.data = it.monitor.monitorInfo.uptime.upHour.toFloat()
                uptime.unit = "H"
            } else if (it.monitor.monitorInfo.uptime.upMin != 0) {
                uptime.data = it.monitor.monitorInfo.uptime.upMin.toFloat()
                uptime.unit = "M"
            } else {
                uptime.data = it.monitor.monitorInfo.uptime.upSec.toFloat()
                uptime.unit = "S"
            }

            uptimeTextView?.text = uptime.data.toInt().toString()
            uptimeUnitTextView?.text = uptime.unit

            loadBar?.setProgress(it.monitor.monitorInfo.loadavg.oneOccupy)
            loadBar?.setProgressSecond(it.monitor.monitorInfo.loadavg.fiveOccupy)
            loadBar?.setProgressThree(it.monitor.monitorInfo.loadavg.fifteenOccupy)


            memFreeTextView?.text = it.monitor.monitorInfo.mem.free.formatPrint()
            memFreeUnitUnitTextView?.text = it.monitor.monitorInfo.mem.freeUnit
            memUsedTextView?.text = it.monitor.monitorInfo.mem.used.formatPrint()
            memUsedUnitUnitTextView?.text = it.monitor.monitorInfo.mem.usedUnit
            memCacheTextView?.text = it.monitor.monitorInfo.mem.cache.formatPrint()
            memCacheUnitUnitTextView?.text = it.monitor.monitorInfo.mem.cacheUnit
            memBar?.setProgress(it.monitor.monitorInfo.mem.usedOccupy)
            memBar?.setProgressSecond(it.monitor.monitorInfo.mem.cacheOccupy)


            netUpSpeedTextView?.text = it.monitor.monitorInfo.netDevs.total.upSpeed.formatPrint()
            netUpSpeedUnitTextView?.text = it.monitor.monitorInfo.netDevs.total.upSpeedUnit
            netDownSpeedTextView?.text = it.monitor.monitorInfo.netDevs.total.downSpeed.formatPrint()
            netDownSpeedUnitTextView?.text = it.monitor.monitorInfo.netDevs.total.downSpeedUnit
            netUpTextView?.text = it.monitor.monitorInfo.netDevs.total.upBytesH.formatPrint()
            netUpUnitTextView?.text = it.monitor.monitorInfo.netDevs.total.upBytesHUnit
            netDownTextView?.text = it.monitor.monitorInfo.netDevs.total.downBytesH.formatPrint()
            netDownUnitTextView?.text = it.monitor.monitorInfo.netDevs.total.downBytesHUnit
            val downOccupy = (100f * it.monitor.monitorInfo.netDevs.total.downBytes.toFloat()) /
                    (it.monitor.monitorInfo.netDevs.total.downBytes.toFloat() + it.monitor.monitorInfo.netDevs.total.upBytes.toFloat())
            netBar?.setProgress(downOccupy)
            netBar?.setProgressSecond(100f - downOccupy)

            netReTranTextView?.text = it.monitor.monitorInfo.netStats.tcp.reTransRate.formatPrint()
            netActiveTextView?.text = it.monitor.monitorInfo.netStats.tcp.activeOpens.toString()
            netPassiveTextView?.text = it.monitor.monitorInfo.netStats.tcp.passiveOpens.toString()
            netFailTextView?.text = it.monitor.monitorInfo.netStats.tcp.failOpens.toString()

            context?.run {
                netDevListView?.adapter = StatusNetDevBaseAdapter(this, it.monitor.monitorInfo.netDevs.devs)
                storeListView?.adapter = StatusStoreBaseAdapter(this, it.monitor.monitorInfo.disks.disks)
            }
        }
    }

}

class StatusStoreBaseAdapter (context: Context, private val disks: List<Disk>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return disks.size
    }

    override fun getItem(index: Int): Any {
        return disks[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: StatusStoreViewHolder?
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_status_store, parent, false)
            holder = StatusStoreViewHolder()
            view.tag = holder

        } else {
            holder = view.tag as StatusStoreViewHolder
        }

        assert(view != null)
        return view!!
    }
}

class StatusStoreViewHolder {

}


class StatusNetDevBaseAdapter (private val context: Context, private val netDevs: List<NetDev>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int {
        return netDevs.size
    }

    override fun getItem(index: Int): Any {
        return netDevs[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: StatusNetDevViewHolder?
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_status_net, parent, false)
            holder = StatusNetDevViewHolder()

            holder.virtualImageView = view.findViewById(R.id.net_virtual_image_view)
            holder.nameTextView = view.findViewById(R.id.net_name_text_view)
            holder.ipTextView = view.findViewById(R.id.net_ip_text_view)

            holder.netUpSpeedTextView = view.findViewById(R.id.net_up_speed_text_view)
            holder.netUpSpeedUnitTextView = view.findViewById(R.id.net_up_speed_unit_text_view)
            holder.netDownSpeedTextView = view.findViewById(R.id.net_down_speed_text_view)
            holder.netDownSpeedUnitTextView = view.findViewById(R.id.net_down_speed_unit_text_view)
            holder.netUpTextView = view.findViewById(R.id.net_up_text_view)
            holder.netUpUnitTextView = view.findViewById(R.id.net_up_unit_text_view)
            holder.netDownTextView = view.findViewById(R.id.net_down_text_view)
            holder.netDownUnitTextView = view.findViewById(R.id.net_down_unit_text_view)
            holder.netBar = view.findViewById(R.id.net_bar)


            view.tag = holder

        } else {
            holder = view.tag as StatusNetDevViewHolder
        }

        val dev = netDevs[index]

        val green = TypedValue()
        context.theme.resolveAttribute(R.attr.highlightGreen, green, true)
        val grey = TypedValue()
        context.theme.resolveAttribute(R.attr.myTextSecondaryColor, grey, true)
        if (!dev.virtual) {

            holder.virtualImageView?.let {
                ImageViewCompat.setImageTintMode(it, PorterDuff.Mode.SRC_IN)
                ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(green.data))
            }

        } else {
            holder.virtualImageView?.let {
                ImageViewCompat.setImageTintMode(it, PorterDuff.Mode.SRC_IN)
                ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(grey.data))
            }
        }

        holder.nameTextView?.text = dev.name

        if (dev.ips.isNotEmpty()) {
            holder.ipTextView?.text = dev.ips[0]
        }

        holder.netUpSpeedTextView?.text = dev.upSpeed.formatPrint()
        holder.netUpSpeedUnitTextView?.text = dev.upSpeedUnit
        holder.netDownSpeedTextView?.text = dev.downSpeed.formatPrint()
        holder.netDownSpeedUnitTextView?.text = dev.downSpeedUnit
        holder.netUpTextView ?.text = dev.upBytesH.formatPrint()
        holder.netUpUnitTextView?.text = dev.upBytesHUnit
        holder.netDownTextView?.text = dev.downBytesH.formatPrint()
        holder.netDownUnitTextView?.text = dev.downBytesHUnit

        if (dev.downBytes != 0L && dev.upBytes != 0L ) {
            val downOccupy = (100f * dev.downBytes.toFloat()) /
                    (dev.downBytes.toFloat() + dev.upBytes.toFloat())
            holder.netBar?.setProgress(downOccupy)
            holder.netBar?.setProgressSecond(100f - downOccupy)
        } else {
            holder.netBar?.setProgress(0f)
            holder.netBar?.setProgressSecond(0f)
        }

        assert(view != null)
        return view!!
    }
}

class StatusNetDevViewHolder {
    var virtualImageView: ImageView? = null
    var nameTextView: TextView? = null
    var ipTextView: TextView? = null

    var netUpSpeedTextView: TextView? = null
    var netUpSpeedUnitTextView: TextView? = null
    var netDownSpeedTextView: TextView? = null
    var netDownSpeedUnitTextView: TextView? = null
    var netUpTextView: TextView? = null
    var netUpUnitTextView: TextView? = null
    var netDownTextView: TextView? = null
    var netDownUnitTextView: TextView? = null
    var netBar: CircleProgress? = null
}


class StatusCpuCoreBaseAdapter (context: Context, private val cpus: List<Cpu>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return cpus.size
    }

    override fun getItem(index: Int): Any {
        return cpus[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: StatusCpuCoreViewHolder?
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_status_cpu_core, parent, false)
            holder = StatusCpuCoreViewHolder()
            holder.vs = LinkedList()
            holder.vs?.apply {
                this.add(view.findViewById(R.id.status_item_cpu_core_0))
                this.add(view.findViewById(R.id.status_item_cpu_core_1))
                this.add(view.findViewById(R.id.status_item_cpu_core_2))
                this.add(view.findViewById(R.id.status_item_cpu_core_3))
                this.add(view.findViewById(R.id.status_item_cpu_core_4))
                this.add(view.findViewById(R.id.status_item_cpu_core_5))
                this.add(view.findViewById(R.id.status_item_cpu_core_6))
                this.add(view.findViewById(R.id.status_item_cpu_core_7))
                this.add(view.findViewById(R.id.status_item_cpu_core_8))
                this.add(view.findViewById(R.id.status_item_cpu_core_9))
                this.add(view.findViewById(R.id.status_item_cpu_core_10))
                this.add(view.findViewById(R.id.status_item_cpu_core_11))
                this.add(view.findViewById(R.id.status_item_cpu_core_12))
                this.add(view.findViewById(R.id.status_item_cpu_core_13))
                this.add(view.findViewById(R.id.status_item_cpu_core_14))
                this.add(view.findViewById(R.id.status_item_cpu_core_15))
                this.add(view.findViewById(R.id.status_item_cpu_core_16))
                this.add(view.findViewById(R.id.status_item_cpu_core_17))
                this.add(view.findViewById(R.id.status_item_cpu_core_18))
                this.add(view.findViewById(R.id.status_item_cpu_core_19))
                this.add(view.findViewById(R.id.status_item_cpu_core_20))
                this.add(view.findViewById(R.id.status_item_cpu_core_21))
                this.add(view.findViewById(R.id.status_item_cpu_core_22))
                this.add(view.findViewById(R.id.status_item_cpu_core_23))
                this.add(view.findViewById(R.id.status_item_cpu_core_24))
                this.add(view.findViewById(R.id.status_item_cpu_core_25))
                this.add(view.findViewById(R.id.status_item_cpu_core_26))
                this.add(view.findViewById(R.id.status_item_cpu_core_27))
                this.add(view.findViewById(R.id.status_item_cpu_core_28))
                this.add(view.findViewById(R.id.status_item_cpu_core_29))
                this.add(view.findViewById(R.id.status_item_cpu_core_30))
                this.add(view.findViewById(R.id.status_item_cpu_core_31))
                this.add(view.findViewById(R.id.status_item_cpu_core_32))
                this.add(view.findViewById(R.id.status_item_cpu_core_33))
            }

            view.tag = holder

        } else {
            holder = view.tag as StatusCpuCoreViewHolder
        }
        val cpu = cpus[index]
        val system = (cpu.system * 0.34).toInt()
        val user = ((cpu.user + cpu.system) * 0.34).toInt()
        val io = ((cpu.ioWait + cpu.system + cpu.user) * 0.34).toInt()
        val nice = ((cpu.nice + cpu.system + cpu.user + cpu.ioWait) * 0.34).toInt()
        assert(nice <= 34)

        assert(view != null)
        val bgRed = AppCompatResources.getDrawable(view!!.context, R.drawable.corners_bg_red)
        val bgGreen = AppCompatResources.getDrawable(view.context, R.drawable.corners_bg_green)
        val bgPurple = AppCompatResources.getDrawable(view.context, R.drawable.corners_bg_purple)
        val bgYellow = AppCompatResources.getDrawable(view.context, R.drawable.corners_bg_yellow)

        holder.vs?.forEachIndexed { i, textView ->
            when (i) {
                in 0 until system -> textView.background = bgRed
                in system until user -> textView.background = bgGreen
                in user until io -> textView.background = bgPurple
                in io until nice -> textView.background = bgYellow
            }
        }
        return view
    }
}

class StatusCpuCoreViewHolder {
    var vs: MutableList<TextView>? = null
}