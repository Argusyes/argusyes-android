package com.android.argusyes.ssh

import android.util.Log
import com.android.argusyes.dao.entity.Server
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*


enum class ConnectStatus {
    INIT, SUCCESS, FAIL
}

const val ROUND_FLOAT2 = "%.2f"

class Monitor (var server: Server){

    var connectStatus = ConnectStatus.INIT
    val monitorInfo: MonitorInfo = MonitorInfo()
    private var jobs = LinkedList<Job>()

    @OptIn(DelicateCoroutinesApi::class)
    private var job: Job = GlobalScope.launch(Dispatchers.IO) {
        try {
            val session = JSch().getSession(server.userName, server.host, server.port).apply {
                setPassword(server.password)

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                setConfig(config)

                connect(5000)
            }

            if (!session.isConnected) {
                connectStatus = ConnectStatus.FAIL
                return@launch
            }

            val sftp = session.openChannel("sftp").apply {
                connect(5000)
            } as ChannelSftp

            if (!sftp.isConnected) {
                connectStatus = ConnectStatus.FAIL
                return@launch
            }

            connectStatus = ConnectStatus.SUCCESS

            jobs.add(parse(sftp, ::parseCpu, "/proc/stat"))
            jobs.forEach { it.join() }

            connectStatus = ConnectStatus.FAIL

            sftp.exit()
            session.disconnect()
        } catch (e : Exception) {
            Log.d("Monitor", e.message?: "error")
            connectStatus = ConnectStatus.FAIL
        }
    }

    fun stopMonitor() {
        jobs.forEach{it.cancel()}
        job.cancel()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun parse(sftp: ChannelSftp, func: (MonitorContext) -> Unit, where: String): Job {
        val context = MonitorContext(sftp, old = "", new = "", oldTime = LocalDateTime.now(), newTime = LocalDateTime.now(), where)
        return GlobalScope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    func(context)
                    context.newToOld()
                    delay(2000)
                }
            } catch (e : Exception) {
                Log.d("parse", e.message?: "error")
            }
        }
    }

    private fun parseCpu(context: MonitorContext) {
        context.getNew()
        if (context.old.isBlank()) {
            context.newToOld()
            context.getNew()
        }
        val reg = """cpu(\d*)\s+(\d+) (\d+) (\d+) (\d+) (\d+) (\d+) (\d+) (\d+) (\d+) (\d+)\n""".toRegex()
        val old = reg.findAll(context.old).map { it.groupValues }.toList()
        val new = reg.findAll(context.new).map { it.groupValues }.toList()

        if(old.isEmpty() || new.isEmpty()) {
            return
        }

        // CPU 时间
        var newTotalCpuTime = 0.0
        for(i in 2 until new[0].size) {
            newTotalCpuTime += new[0][i].toDouble()
        }

        var oldTotalCpuTime = 0.0
        for(i in 2 until old[0].size) {
            oldTotalCpuTime += old[0][i].toDouble()
        }

        var totalDiffTime = newTotalCpuTime - oldTotalCpuTime
        if (totalDiffTime == 0.0) {
            totalDiffTime++
        }

        monitorInfo.cpus.total.utilization = ROUND_FLOAT2.format(100.0 - 100.0*(new[0][5].toDouble() - old[0][5].toDouble()) / totalDiffTime).toDouble()
        monitorInfo.cpus.total.free = ROUND_FLOAT2.format(100.0*(new[0][5].toDouble() - old[0][5].toDouble()) / totalDiffTime).toDouble()
        monitorInfo.cpus.total.system = ROUND_FLOAT2.format(100.0*(new[0][4].toDouble() - old[0][4].toDouble()) / totalDiffTime).toDouble()
        monitorInfo.cpus.total.user = ROUND_FLOAT2.format(100.0*(new[0][2].toDouble() - old[0][2].toDouble()) / totalDiffTime).toDouble()
        monitorInfo.cpus.total.ioWait = ROUND_FLOAT2.format(100.0*(new[0][6].toDouble() - old[0][6].toDouble()) / totalDiffTime).toDouble()
        monitorInfo.cpus.total.steal = ROUND_FLOAT2.format(100.0*(new[0][9].toDouble() - old[0][9].toDouble()) / totalDiffTime).toDouble()

        for (i in 1 until old.size) {
            var pNewTotalCpuTime = 0.0
            for(j in 2 until new[i].size) {
                pNewTotalCpuTime += new[i][j].toDouble()
            }

            var pOldTotalCpuTime = 0.0
            for(j in 2 until old[i].size) {
                pOldTotalCpuTime += old[i][j].toDouble()
            }

            var pTotalDiffTime = pNewTotalCpuTime - pOldTotalCpuTime
            if (pTotalDiffTime == 0.0) {
                pTotalDiffTime++
            }
            val processor = old[i][1].toInt()
            val cpu = monitorInfo.cpus.map.getOrDefault(processor, Cpu())
            cpu.processor = processor
            cpu.utilization = ROUND_FLOAT2.format(100.0 - 100.0*(new[i][5].toDouble() - old[i][5].toDouble()) / pTotalDiffTime).toDouble()
            cpu.free = ROUND_FLOAT2.format(100.0*(new[i][5].toDouble() - old[i][5].toDouble()) / pTotalDiffTime).toDouble()
            cpu.system = ROUND_FLOAT2.format(100.0*(new[i][4].toDouble() - old[i][4].toDouble()) / pTotalDiffTime).toDouble()
            cpu.user = ROUND_FLOAT2.format(100.0*(new[i][2].toDouble() - old[i][2].toDouble()) / pTotalDiffTime).toDouble()
            cpu.ioWait = ROUND_FLOAT2.format(100.0*(new[i][6].toDouble() - old[i][6].toDouble()) / pTotalDiffTime).toDouble()
            cpu.steal = ROUND_FLOAT2.format(100.0*(new[i][9].toDouble() - old[i][9].toDouble()) / pTotalDiffTime).toDouble()
            monitorInfo.cpus.map[processor] = cpu
        }
    }
}

class MonitorContext(
    private val sftp: ChannelSftp,
    var old: String,
    var new: String,
    var oldTime: LocalDateTime,
    var newTime: LocalDateTime,
    var where: String) {

    fun readFile(fileName : String): String {
        val result = ByteArrayOutputStream()
        synchronized(sftp) {
            sftp.get(fileName, result)
        }
        return result.toString(StandardCharsets.UTF_8.name())
    }

    fun newToOld() {
        this.old = this.new
        this.oldTime = this.newTime
        this.new = ""
    }

    fun getNew() {
        if (where.isBlank()) {
            return
        }
        new = readFile(where)
        newTime = LocalDateTime.now()
    }
}
