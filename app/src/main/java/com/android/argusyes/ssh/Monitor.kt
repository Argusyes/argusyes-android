package com.android.argusyes.ssh

import android.util.Log
import com.android.argusyes.dao.entity.Server
import com.jcraft.jsch.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


enum class ConnectStatus {
    INIT, SUCCESS, FAIL
}

const val ROUND_FLOAT2 = "%.2f"

data class UnitData (var data: Float, var unit: String)

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

    private fun roundSpeed(b: Long): UnitData {
        val u = roundMem(b)
        u.unit += "/S"
        return u
    }

    private fun roundMem(b: Long): UnitData {
        return if (b > 1024L*1024*1024*1024) {
            UnitData(ROUND_FLOAT2.format(b.toDouble()/1024/1024/1024/1024).toFloat(), "T")
        } else if (b > 1024L*1024*1024) {
            UnitData(ROUND_FLOAT2.format(b.toDouble()/1024/1024/1024).toFloat(), "G")
        } else if (b > 1024*1024) {
            UnitData(ROUND_FLOAT2.format(b.toDouble()/1024/1024).toFloat(), "M")
        } else if (b > 1024) {
            UnitData(ROUND_FLOAT2.format(b.toDouble()/1024).toFloat(), "K")
        } else {
            UnitData(ROUND_FLOAT2.format(b.toDouble()).toFloat(), "B")
        }
    }

    private fun intToBytesBig(value: Int):List<UByte> {
        return listOf((value and 0xFF).toUByte(), ((value shr 8) and 0xFF).toUByte(),
            ((value shr 16) and 0xFF).toUByte(), ((value shr 24) and 0xFF).toUByte())
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun parse(sftp: ChannelSftp, func: (MonitorContext) -> Unit, where: String): Job {
        val context = MonitorContext(sftp, old = "", new = "", oldTime = LocalDateTime.now(), newTime = LocalDateTime.now(), where)
        return GlobalScope.launch(Dispatchers.IO) {

            while (isActive) {
                try {
                    func(context)
                    context.newToOld()
                    Log.d("parse:${context.where}", "success")
                } catch (e: Exception) {
                    Log.d("parse:${context.where}", e.message ?: "error")
                } finally {
                    delay(2000)
                }
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
        monitorInfo.cpus.total.nice = ROUND_FLOAT2.format(100.0*(new[0][3].toDouble() - old[0][3].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.user = ROUND_FLOAT2.format(100.0*(new[0][2].toDouble() - old[0][2].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.ioWait = ROUND_FLOAT2.format(100.0*(new[0][6].toDouble() - old[0][6].toDouble()) / totalDiffTime).toFloat()
        monitorInfo.cpus.total.steal = ROUND_FLOAT2.format(100.0*(new[0][9].toDouble() - old[0][9].toDouble()) / totalDiffTime).toFloat()

        val cpuList = LinkedList<Cpu>()
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
            val cpu = Cpu()
            cpu.processor = processor
            cpu.utilization = ROUND_FLOAT2.format(100.0 - 100.0*(new[i][5].toDouble() - old[i][5].toDouble()) / pTotalDiffTime).toFloat()
            cpu.free = ROUND_FLOAT2.format(100.0*(new[i][5].toDouble() - old[i][5].toDouble()) / pTotalDiffTime).toFloat()
            cpu.system = ROUND_FLOAT2.format(100.0*(new[i][4].toDouble() - old[i][4].toDouble()) / pTotalDiffTime).toFloat()
            cpu.nice = ROUND_FLOAT2.format(100.0*(new[i][3].toDouble() - old[i][3].toDouble()) / pTotalDiffTime).toFloat()
            cpu.user = ROUND_FLOAT2.format(100.0*(new[i][2].toDouble() - old[i][2].toDouble()) / pTotalDiffTime).toFloat()
            cpu.ioWait = ROUND_FLOAT2.format(100.0*(new[i][6].toDouble() - old[i][6].toDouble()) / pTotalDiffTime).toFloat()
            cpu.steal = ROUND_FLOAT2.format(100.0*(new[i][9].toDouble() - old[i][9].toDouble()) / pTotalDiffTime).toFloat()

            cpuList.add(cpu)
        }

        cpuList.sortBy { it.processor }
        monitorInfo.cpus.cpus = cpuList

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
        if (monitorInfo.cpus.cpus.isEmpty()) {
            return
        }
        context.getNew()
        val processorNum = monitorInfo.cpus.cpus.size
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
        context.getNew()
        if (context.old.isBlank()) {
            context.newToOld()
            context.getNew()
        }

        val reg = """([^:\n]+):\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\n""".toRegex()
        val old = reg.findAll(context.old).map { it.groupValues }.toList()
        val new = reg.findAll(context.new).map { it.groupValues }.toList()

        if(old.isEmpty() || new.isEmpty()) {
            return
        }

        val route = context.readFile("/proc/net/route")
        val fib = context.readFile("/proc/net/fib_trie")

        val ipMap = route.split("\n")
            // 去掉表头
            .filterIndexed {index, s -> index != 0 && s.isNotBlank() }
            // 每行
            .map { it.split("\t") }
            // 网关为0
            .filter { it[2].toInt(16) == 0 }
            // 按名字分组
            .groupBy { it[0] }
            .mapValues {
                // 每组列表
                it.value.map { l ->
                    // 转化为 ip 前缀
                    val ip = intToBytesBig(l[1].toInt(16))
                    "${ip[0]}.${ip[1]}.${ip[2]}.${ip[3]}"
                }.filter { s ->
                    // 去除本地
                    !s.startsWith("""169.254""")
                }.map { s ->
                    // 查找真正 ip
                    val fibReg = ("""$s([^L]*)\n\s+/32 host LOCAL\n""").toRegex()
                    val fibResult = fibReg.findAll(fib).map { r -> r.groupValues }.toList()
                    """.* (\d+\.\d+\.\d+\.\d+)${'$'}""".toRegex().findAll(fibResult[0][1]).map { r -> r.groupValues }.toList()[0][1]
                }.toSet()
            }

        var diffTime = Duration.between(context.oldTime, context.newTime).toMillis()
        if (diffTime == 0L) diffTime++

        val oldMap = old.associateBy { it[1].trim() }
        var oldTotalUpBytes = 0L
        var oldTotalDownBytes = 0L
        val total = NetDev()
        val devList = LinkedList<NetDev>()
        for (it in new) {
            val name = it[1].trim()
            val oldIt = oldMap[name] ?: continue
            val dev = NetDev()
            dev.name = name
            dev.ips = LinkedList(ipMap[name]?: setOf())
            dev.virtual = true
            try {
                context.stat("""/sys/devices/virtual/net/$name""")
            } catch (e : SftpException) {
                dev.virtual = false
            }
            dev.downBytes = it[2].toLong()
            dev.downPackets = it[3].toLong()
            dev.upBytes = it[10].toLong()
            dev.upPackets = it[11].toLong()
            val downBytesRoundedMem = roundMem(dev.downBytes)
            dev.downBytesH = downBytesRoundedMem.data
            dev.downBytesHUnit = downBytesRoundedMem.unit
            val upBytesRoundedMem = roundMem(dev.upBytes)
            dev.upBytesH = upBytesRoundedMem.data
            dev.upBytesHUnit = upBytesRoundedMem.unit

            val oldDownBytes = oldIt[2].toLong()
            val oldUpBytes = oldIt[10].toLong()

            val downSpeedRoundedSpeed = roundSpeed((dev.downBytes - oldDownBytes) * 1000 / diffTime)
            dev.downSpeed = downSpeedRoundedSpeed.data
            dev.downSpeedUnit = downSpeedRoundedSpeed.unit

            val upSpeedRoundedSpeed = roundSpeed((dev.upBytes - oldUpBytes) * 1000 / diffTime)
            dev.upSpeed = upSpeedRoundedSpeed.data
            dev.upSpeedUnit = upSpeedRoundedSpeed.unit
            if (!dev.virtual) {
                oldTotalDownBytes += oldDownBytes
                oldTotalUpBytes += oldUpBytes
                total.upBytes += dev.upBytes
                total.downBytes += dev.downBytes
                total.upPackets += dev.upPackets
                total.downPackets += dev.downPackets
            }
            devList.add(dev)
        }

        val downBytesRoundedMem = roundMem(total.downBytes)
        total.downBytesH = downBytesRoundedMem.data
        total.downBytesHUnit = downBytesRoundedMem.unit
        val upBytesRoundedMem = roundMem(total.upBytes)
        total.upBytesH = upBytesRoundedMem.data
        total.upBytesHUnit = upBytesRoundedMem.unit
        val downSpeedRoundedSpeed = roundMem((total.downBytes - oldTotalDownBytes) * 1000 / diffTime)
        total.downSpeed = downSpeedRoundedSpeed.data
        total.downSpeedUnit = downSpeedRoundedSpeed.unit
        val upSpeedRoundedSpeed = roundMem((total.upBytes - oldTotalUpBytes) * 1000 / diffTime)
        total.upSpeed = upSpeedRoundedSpeed.data
        total.upSpeedUnit = upSpeedRoundedSpeed.unit

        devList.sortBy { it.name }
        devList.sortBy { it.virtual }
        monitorInfo.netDevs.devs = devList
        monitorInfo.netDevs.total = total
    }

    private fun parseNetStat(context: MonitorContext) {
        context.getNew()
        val tcpReg = """Tcp:\s+(\d+)\s+(\d+)\s+(\d+)\s+([\d-]+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\n""".toRegex()
        val tcp = tcpReg.findAll(context.new).map { r -> r.groupValues }.toList()

        val udpReg = """Udp:\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)[^\n]+\n""".toRegex()
        val udp = udpReg.findAll(context.new).map { r -> r.groupValues }.toList()

        monitorInfo.netStats.tcp.activeOpens = tcp[0][5].toInt()
        monitorInfo.netStats.tcp.passiveOpens = tcp[0][6].toInt()
        monitorInfo.netStats.tcp.failOpens = tcp[0][7].toInt()
        monitorInfo.netStats.tcp.currConn = tcp[0][9].toInt()
        monitorInfo.netStats.tcp.inSegments = tcp[0][10].toInt()
        monitorInfo.netStats.tcp.outSegments = tcp[0][11].toInt()
        monitorInfo.netStats.tcp.reTransSegments = tcp[0][12].toInt()
        if (monitorInfo.netStats.tcp.outSegments != 0) {
            monitorInfo.netStats.tcp.reTransRate = ROUND_FLOAT2.format(100f * monitorInfo.netStats.tcp.reTransSegments.toFloat() / monitorInfo.netStats.tcp.outSegments.toFloat()).toFloat()
        }

        monitorInfo.netStats.udp.inDatagrams = udp[0][1].toInt()
        monitorInfo.netStats.udp.outDatagrams = udp[0][4].toInt()
        monitorInfo.netStats.udp.receiveBufErrors = udp[0][5].toInt()
        monitorInfo.netStats.udp.sendBufErrors = udp[0][6].toInt()
    }

    private fun parseTemp(context: MonitorContext) {
        context.getNew()
        monitorInfo.temp.map["zone0"] = context.new.trim().toInt() / 1000
    }

    private fun parseDisk(context: MonitorContext) {
        context.getNew()
        if (context.old.isBlank()) {
            context.newToOld()
            context.getNew()
        }

        val mount = context.readFile("/proc/mounts")
        val mountRegResult = """(\S+) (\S+) (\S+) (\S+) (\d+) (\d+)\n""".toRegex().findAll(mount).map { r -> r.groupValues }.toList()
        val mountMap = mountRegResult.filter { it[3] == "ext4" || it[3] == "vfat" }.associateBy { it[1].trim() }

        val diskReg = """(\d+)\s+(\d+)\s+(\S+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)[^\n]+\n""".toRegex()
        val old = diskReg.findAll(context.old).map { r -> r.groupValues }.toList()
        val new = diskReg.findAll(context.new).map { r -> r.groupValues }.toList()
        var diffTime = Duration.between(context.oldTime, context.newTime).toMillis()
        if (diffTime == 0L) diffTime++
        val oldDiskMap = old.filter { mountMap.containsKey("""/dev/${it[3].trim()}""") } .associateBy { """/dev/${it[3].trim()}""" }
        val newDiskMap = new.filter { mountMap.containsKey("""/dev/${it[3].trim()}""") } .associateBy { """/dev/${it[3].trim()}""" }

        val sectorSize = 512L
        var oldTotalWrite = 0L
        var newTotalWrite = 0L
        var oldTotalRead = 0L
        var newTotalRead = 0L
        val diskList = LinkedList<Disk>()
        for (mountEntry in mountMap) {
            val mountV = mountEntry.value
            val oldV = oldDiskMap[mountEntry.key]?: continue
            val newV = newDiskMap[mountEntry.key]?: continue
            val d = Disk()
            d.mount = mountV[2]
            d.fileSystem = mountV[3]
            val stat = context.statVFS(d.mount)
            val totalRounded = roundMem(stat.size * 1024)
            d.total = totalRounded.data
            d.totalUnit = totalRounded.unit
            val freeRounded = roundMem(stat.avail * 1024)
            d.free = freeRounded.data
            d.freeUnit = freeRounded.unit
            d.freeOccupy = ROUND_FLOAT2.format(100f * stat.avail.toFloat() / stat.size.toFloat()).toFloat()
            val usedRounded = roundMem(stat.used * 1024)
            d.used = usedRounded.data
            d.usedUnit = usedRounded.unit
            d.usedOccupy = ROUND_FLOAT2.format(100f * stat.used.toFloat() / stat.size.toFloat()).toFloat()

            val oldWrite = sectorSize * oldV[10].toLong()
            val newWrite = sectorSize * newV[10].toLong()
            val writeRounded = roundMem(newWrite)
            d.write = writeRounded.data
            d.writeUnit = writeRounded.unit
            val writeSpeedRounded = roundSpeed((newWrite - oldWrite) * 1000 / diffTime)
            d.writeSpeed = writeSpeedRounded.data
            d.writeSpeedUnit = writeSpeedRounded.unit

            val oldRead = sectorSize * oldV[6].toLong()
            val newRead = sectorSize * newV[6].toLong()
            val readRounded = roundMem(newRead)
            d.read = readRounded.data
            d.readUnit = readRounded.unit
            val readSpeedRounded = roundSpeed((newRead - oldRead) * 1000 / diffTime)
            d.readSpeed = readSpeedRounded.data
            d.readSpeedUnit = readSpeedRounded.unit

            oldTotalWrite += oldWrite
            newTotalWrite += newWrite
            oldTotalRead += oldRead
            newTotalRead += newRead

            d.writeIOPS = ((newV[8].toInt() - oldV[8].toInt()) * 1000 / diffTime).toInt()
            d.readIOPS = ((newV[4].toInt() - oldV[4].toInt()) * 1000 / diffTime).toInt()
            diskList.add(d)
        }
        val total = DiskTotal()
        val writeRounded = roundMem(newTotalWrite)
        total.write = writeRounded.data
        total.writeUnit = writeRounded.unit

        val readRounded = roundMem(newTotalRead)
        total.read = readRounded.data
        total.readUnit = readRounded.unit

        val writeSpeedRounded = roundMem((newTotalWrite - oldTotalWrite) * 1000 / diffTime)
        total.writeSpeed = writeSpeedRounded.data
        total.writeSpeedUnit = writeSpeedRounded.unit

        val readSpeedRounded = roundMem((newTotalRead - oldTotalRead) * 1000 / diffTime)
        total.readSpeed = readSpeedRounded.data
        total.readSpeedUnit = readSpeedRounded.unit

        monitorInfo.disks.total = total
        monitorInfo.disks.disks = diskList
    }
}

class MonitorContext(
    private val sftp: ChannelSftp,
    var old: String,
    var new: String,
    var oldTime: LocalDateTime,
    var newTime: LocalDateTime,
    var where: String) {

    fun statVFS(path: String): SftpStatVFS {
        synchronized(sftp) {
            return sftp.statVFS(path)
        }
    }

    fun stat(path: String): SftpATTRS {
        synchronized(sftp) {
            return sftp.stat(path)
        }
    }

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
