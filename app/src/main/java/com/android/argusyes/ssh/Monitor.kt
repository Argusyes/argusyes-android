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

data class MemRounded (var data: Float, var unit: String)

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
            jobs.add(parse(sftp, ::parseMem, "/proc/meminfo"))
            jobs.add(parse(sftp, ::parseUptime, "/proc/uptime"))
            jobs.add(parse(sftp, ::parseLoadavg, "/proc/loadavg"))
            jobs.add(parse(sftp, ::parseNetDev, "/proc/net/dev"))
            jobs.add(parse(sftp, ::parseNetStat, "/proc/net/snmp"))
            jobs.add(parse(sftp, ::parseTemp, "/sys/class/thermal/thermal_zone0/temp"))
            jobs.add(parse(sftp, ::parseDisk, "/proc/diskstats"))
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

    private fun roundMem(b: Long): MemRounded {
        return if (b > 1024L*1024*1024*1024) {
            MemRounded(ROUND_FLOAT2.format(b.toDouble()/1024/1024/1024/1024).toFloat(), "T")
        } else if (b > 1024L*1024*1024) {
            MemRounded(ROUND_FLOAT2.format(b.toDouble()/1024/1024/1024).toFloat(), "G")
        } else if (b > 1024*1024) {
            MemRounded(ROUND_FLOAT2.format(b.toDouble()/1024/1024).toFloat(), "M")
        } else if (b > 1024) {
            MemRounded(ROUND_FLOAT2.format(b.toDouble()/1024).toFloat(), "K")
        } else {
            MemRounded(ROUND_FLOAT2.format(b.toDouble()).toFloat(), "B")
        }
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
                Log.d("parse:${context.where}", e.message?: "error")
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

        monitorInfo.cpus.total.utilization = ROUND_FLOAT2.format(100.0 - 100.0*(new[0][5].toDouble() - old[0][5].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.free = ROUND_FLOAT2.format(100.0*(new[0][5].toDouble() - old[0][5].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.system = ROUND_FLOAT2.format(100.0*(new[0][4].toDouble() - old[0][4].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.user = ROUND_FLOAT2.format(100.0*(new[0][2].toDouble() - old[0][2].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.ioWait = ROUND_FLOAT2.format(100.0*(new[0][6].toDouble() - old[0][6].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.steal = ROUND_FLOAT2.format(100.0*(new[0][9].toDouble() - old[0][9].toDouble()) / totalDiffTime).toFloat()

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
            cpu.utilization = ROUND_FLOAT2.format(100.0 - 100.0*(new[i][5].toDouble() - old[i][5].toDouble()) / pTotalDiffTime).toFloat()
            cpu.free = ROUND_FLOAT2.format(100.0*(new[i][5].toDouble() - old[i][5].toDouble()) / pTotalDiffTime).toFloat()
            cpu.system = ROUND_FLOAT2.format(100.0*(new[i][4].toDouble() - old[i][4].toDouble()) / pTotalDiffTime).toFloat()
            cpu.user = ROUND_FLOAT2.format(100.0*(new[i][2].toDouble() - old[i][2].toDouble()) / pTotalDiffTime).toFloat()
            cpu.ioWait = ROUND_FLOAT2.format(100.0*(new[i][6].toDouble() - old[i][6].toDouble()) / pTotalDiffTime).toFloat()
            cpu.steal = ROUND_FLOAT2.format(100.0*(new[i][9].toDouble() - old[i][9].toDouble()) / pTotalDiffTime).toFloat()
            monitorInfo.cpus.map[processor] = cpu
        }
    }

    private fun parseMem(context: MonitorContext) {
        context.getNew()
        val totalMemReg = """MemTotal:\D+(\d+) kB\n""".toRegex()
        val totalMem = totalMemReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedTotalMem = roundMem(totalMem * 1024L)
        monitorInfo.mem.total = roundedTotalMem.data
        monitorInfo.mem.totalUnit = roundedTotalMem.unit

        val totalSwapReg = """SwapTotal:\D+(\d+) kB\n""".toRegex()
        val totalSwap = totalSwapReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedTotalSwap = roundMem(totalSwap * 1024L)
        monitorInfo.mem.totalSwap = roundedTotalSwap.data
        monitorInfo.mem.totalSwapUnit = roundedTotalSwap.unit

        val freeMemReg = """MemFree:\D+(\d+) kB\n""".toRegex()
        val freeMem = freeMemReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedFreeMem = roundMem(freeMem * 1024L)
        monitorInfo.mem.free = roundedFreeMem.data
        monitorInfo.mem.freeUnit = roundedFreeMem.unit
        monitorInfo.mem.freeOccupy = ROUND_FLOAT2.format(100*freeMem.toDouble()/totalMem.toDouble()).toFloat()

        val availableMemReg = """MemAvailable:\D+(\d+) kB\n""".toRegex()
        val availableMem = availableMemReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedAvailableMem = roundMem(availableMem * 1024L)
        monitorInfo.mem.available = roundedAvailableMem.data
        monitorInfo.mem.availableUnit = roundedAvailableMem.unit
        monitorInfo.mem.availableOccupy = ROUND_FLOAT2.format(100*availableMem.toDouble()/totalMem.toDouble()).toFloat()

        val usedMem = totalMem - availableMem
        val roundedUsedMem = roundMem(usedMem * 1024L)
        monitorInfo.mem.used = roundedUsedMem.data
        monitorInfo.mem.usedUnit = roundedUsedMem.unit
        monitorInfo.mem.usedOccupy = ROUND_FLOAT2.format(100*usedMem.toDouble()/totalMem.toDouble()).toFloat()

        val bufferMemReg = """Buffers:\D+(\d+) kB\n""".toRegex()
        val bufferMem = bufferMemReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedBufferMem = roundMem(bufferMem * 1024L)
        monitorInfo.mem.buffer = roundedBufferMem.data
        monitorInfo.mem.bufferUnit = roundedBufferMem.unit
        monitorInfo.mem.bufferOccupy = ROUND_FLOAT2.format(100*bufferMem.toDouble()/totalMem.toDouble()).toFloat()

        val cachedMemReg = """Cached:\D+(\d+) kB\n""".toRegex()
        val cachedMem = cachedMemReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedCachedMem = roundMem(cachedMem * 1024L)
        monitorInfo.mem.cache = roundedCachedMem.data
        monitorInfo.mem.cacheUnit = roundedCachedMem.unit
        monitorInfo.mem.cacheOccupy = ROUND_FLOAT2.format(100*cachedMem.toDouble()/totalMem.toDouble()).toFloat()

        val dirtyMemReg = """Dirty:\D+(\d+) kB\n""".toRegex()
        val dirtyMem = dirtyMemReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedDirtyMem = roundMem(dirtyMem * 1024L)
        monitorInfo.mem.dirty = roundedDirtyMem.data
        monitorInfo.mem.dirtyUnit = roundedDirtyMem.unit
        monitorInfo.mem.dirtyOccupy = ROUND_FLOAT2.format(100*dirtyMem.toDouble()/totalMem.toDouble()).toFloat()

        val cachedSwapReg = """SwapCached:\D+(\d+) kB\n""".toRegex()
        val cachedSwap = cachedSwapReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedCachedSwap = roundMem(cachedSwap * 1024L)
        monitorInfo.mem.cachedSwap = roundedCachedSwap.data
        monitorInfo.mem.cachedSwapUnit = roundedCachedSwap.unit
        monitorInfo.mem.cachedSwapOccupy = ROUND_FLOAT2.format(100*cachedSwap.toDouble()/totalSwap.toDouble()).toFloat()

        val freeSwapReg = """SwapFree:\D+(\d+) kB\n""".toRegex()
        val freeSwap = freeSwapReg.findAll(context.new).map { it.groupValues } .toList()[0][1].toLong()
        val roundedFreeSwap = roundMem(freeSwap * 1024L)
        monitorInfo.mem.freeSwap = roundedFreeSwap.data
        monitorInfo.mem.freeSwapUnit = roundedFreeSwap.unit
        monitorInfo.mem.freeSwapOccupy = ROUND_FLOAT2.format(100*freeSwap.toDouble()/totalSwap.toDouble()).toFloat()

        val usedSwap = totalSwap - freeSwap - cachedSwap
        val roundedUsedSwap = roundMem(usedSwap * 1024L)
        monitorInfo.mem.usedSwap = roundedUsedSwap.data
        monitorInfo.mem.usedSwapUnit = roundedUsedSwap.unit
        monitorInfo.mem.usedSwapOccupy = ROUND_FLOAT2.format(100*usedSwap.toDouble()/totalSwap.toDouble()).toFloat()

    }

    private fun parseUptime(context: MonitorContext) {
        context.getNew()
        var uptime = context.new.split(" ")[0].toFloat().toInt()
        monitorInfo.uptime.upDay = uptime / (60*60*24)
        uptime %= (60 * 60 * 24)
        monitorInfo.uptime.upHour = uptime / (60*60)
        uptime %= (60 * 60)
        monitorInfo.uptime.upMin = uptime / (60)
        uptime %= (60)
        monitorInfo.uptime.upSec = uptime
    }

    private fun parseLoadavg(context: MonitorContext) {
        if (monitorInfo.cpus.map.isEmpty()) {
            return
        }
        context.getNew()
        val processorNum = monitorInfo.cpus.map.size
        val ss = context.new.replace("\n", "").split(" ")
        monitorInfo.loadavg.one = ss[0].toFloat()
        monitorInfo.loadavg.oneOccupy = ROUND_FLOAT2.format(100 * monitorInfo.loadavg.one / processorNum).toFloat()
        monitorInfo.loadavg.five = ss[1].toFloat()
        monitorInfo.loadavg.fiveOccupy = ROUND_FLOAT2.format(100 * monitorInfo.loadavg.five / processorNum).toFloat()
        monitorInfo.loadavg.fifteen = ss[2].toFloat()
        monitorInfo.loadavg.fifteenOccupy = ROUND_FLOAT2.format(100 * monitorInfo.loadavg.fifteen / processorNum).toFloat()

        val thread = ss[3].split("/")
        monitorInfo.loadavg.running = thread[0].toInt()
        monitorInfo.loadavg.active = thread[1].toInt()
        monitorInfo.loadavg.lastPid = ss[4].toInt()

    }

    private fun parseNetDev(context: MonitorContext) {

    }

    private fun parseNetStat(context: MonitorContext) {

    }

    private fun parseTemp(context: MonitorContext) {

    }

    private fun parseDisk(context: MonitorContext) {

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
