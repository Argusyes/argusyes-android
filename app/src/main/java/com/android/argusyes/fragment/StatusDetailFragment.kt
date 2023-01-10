package com.android.argusyes.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.android.argusyes.R
import com.android.argusyes.ssh.Cpu
import com.android.argusyes.ssh.Disk
import com.android.argusyes.ssh.NetDev
import com.android.argusyes.ssh.SSHManager
import com.android.argusyes.ui.ListViewForScrollView
import java.util.*

class StatusDetailFragment : Fragment() {

    private var serverId: String? = null

    private var sshManager: SSHManager? = null

    private var cpuCoreListView: ListViewForScrollView? = null
    private var netDevListView: ListViewForScrollView? = null
    private var storeListView: ListViewForScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sshManager = context?.let { SSHManager.getInstance(it) }
        arguments?.let {
            serverId = it.getString(SERVER_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status_detail, container, false)
        cpuCoreListView = view.findViewById(R.id.status_detail_cpu_core_list_view)
        netDevListView = view.findViewById(R.id.status_detail_net_dev_list_view)
        storeListView = view.findViewById(R.id.status_detail_store_list_view)

        context?.run {
            cpuCoreListView?.adapter = StatusCpuCoreBaseAdapter(this, getFakeCpuCore())
            netDevListView?.adapter = StatusNetDevBaseAdapter(this, getFakeNetDev())
            storeListView?.adapter = StatusStoreBaseAdapter(this, getFakeDisk())
        }

        val netDevHeaderView = inflater.inflate(R.layout.item_status_net_header, container, false)
        netDevListView?.addHeaderView(netDevHeaderView)
        return view
    }
}

private fun getFakeDisk(): List<Disk> {
    val res = LinkedList<Disk>()
    res.add(Disk("1", "/ff", "aa", 2f, 2f, "M"))
    res.add(Disk("1", "/ff", "aa", 2f, 2f, "M"))
    res.add(Disk("1", "/ff", "aa", 2f, 2f, "M"))
    res.add(Disk("1", "/ff", "aa", 2f, 2f, "M"))
    return res
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

private fun getFakeNetDev(): List<NetDev> {
    val res = LinkedList<NetDev>()
    res.add(NetDev("1", listOf("192.168.0.1"), true, 0f, "M", 2, 0f, "M", 2))
    res.add(NetDev("1", listOf("192.168.0.1"), true, 0f, "M", 2, 0f, "M", 2))
    res.add(NetDev("1", listOf("192.168.0.1"), true, 0f, "M", 2, 0f, "M", 2))
    res.add(NetDev("1", listOf("192.168.0.1"), true, 0f, "M", 2, 0f, "M", 2))
    res.add(NetDev("1", listOf("192.168.0.1"), true, 0f, "M", 2, 0f, "M", 2))
    return res
}

class StatusNetDevBaseAdapter (context: Context, private val netDevs: List<NetDev>) : BaseAdapter() {

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
            view.tag = holder

        } else {
            holder = view.tag as StatusNetDevViewHolder
        }

        assert(view != null)
        return view!!
    }
}

class StatusNetDevViewHolder {

}

private fun getFakeCpuCore(): List<Cpu> {
    val res = LinkedList<Cpu>()
    res.add(Cpu(0, 0.96f, 0.45f, 0f, 0.3f, 0.2f, 0.2f, 0.16f))
    res.add(Cpu(1, 0.96f, 0.45f, 0.1f, 0f, 0.2f, 0.2f, 0.16f))
    res.add(Cpu(2, 0.96f, 0.45f, 0.1f, 0.3f, 0f, 0.2f, 0.16f))
    res.add(Cpu(3, 0.96f, 0.45f, 0.1f, 0.3f, 0.2f, 0f, 0.16f))
    res.add(Cpu(4, 0.96f, 0.45f, 0.4f, 0.3f, 0.2f, 0.1f, 0.16f))
    res.add(Cpu(5, 0.96f, 0.45f, 0.4f, 0.2f, 0.1f, 0.1f, 0.16f))
    res.add(Cpu(6, 0.96f, 0.45f, 0.5f, 0.1f, 0.1f, 0.1f, 0.16f))
    res.add(Cpu(7, 0.96f, 0.45f, 0.1f, 0.1f, 0.1f, 0.5f, 0.16f))
    return res
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
        val system = (cpu.system * 34).toInt()
        val user = ((cpu.user + cpu.system) * 34).toInt()
        val io = ((cpu.ioWait + cpu.system + cpu.user) * 34).toInt()
        val nice = ((cpu.nice + cpu.system + cpu.user + cpu.ioWait) * 34).toInt()
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