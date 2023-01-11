package com.android.argusyes.ssh

import java.util.LinkedList

data class MonitorInfo (
    @Volatile var cpus: Cpus = Cpus(),
    @Volatile var mem: Mem = Mem(),
    @Volatile var uptime: Uptime = Uptime(),
    @Volatile var loadavg: Loadavg = Loadavg(),
    @Volatile var netDevs: NetDevs = NetDevs(),
    @Volatile var netStats: NetStats = NetStats(),
    @Volatile var temp: Temp = Temp(),
    @Volatile var disks: Disks = Disks(),
)

data class Cpus(
    @Volatile var cpus: MutableList<Cpu> = LinkedList(),
    @Volatile var total: Cpu = Cpu(),
)

data class Cpu(
    @Volatile var processor: Int = 0,
    @Volatile var utilization: Float = 0f,
    @Volatile var free: Float = 0f,
    @Volatile var system: Float = 0f,
    @Volatile var user: Float = 0f,
    @Volatile var nice: Float = 0f,
    @Volatile var ioWait: Float = 0f,
    @Volatile var steal: Float = 0f,
)

data class Mem(
    @Volatile var total: Float = 0f,
    @Volatile var totalUnit: String = "",
    @Volatile var free: Float = 0f,
    @Volatile var freeOccupy: Float = 0f,
    @Volatile var freeUnit: String = "",
    @Volatile var available: Float = 0f,
    @Volatile var availableOccupy: Float = 0f,
    @Volatile var availableUnit: String = "",
    @Volatile var used: Float = 0f,
    @Volatile var usedOccupy: Float = 0f,
    @Volatile var usedUnit: String = "",
    @Volatile var buffer: Float = 0f,
    @Volatile var bufferOccupy: Float = 0f,
    @Volatile var bufferUnit: String = "",
    @Volatile var cache: Float = 0f,
    @Volatile var cacheOccupy: Float = 0f,
    @Volatile var cacheUnit: String = "",
    @Volatile var dirty: Float = 0f,
    @Volatile var dirtyOccupy: Float = 0f,
    @Volatile var dirtyUnit: String = "",
    @Volatile var totalSwap: Float = 0f,
    @Volatile var totalSwapUnit: String = "",
    @Volatile var freeSwap: Float = 0f,
    @Volatile var freeSwapOccupy: Float = 0f,
    @Volatile var freeSwapUnit: String = "",
    @Volatile var cachedSwap: Float = 0f,
    @Volatile var cachedSwapOccupy: Float = 0f,
    @Volatile var cachedSwapUnit: String = "",
    @Volatile var usedSwap: Float = 0f,
    @Volatile var usedSwapOccupy: Float = 0f,
    @Volatile var usedSwapUnit: String = "",
)

data class Uptime(
    @Volatile var upDay: Int = 0,
    @Volatile var upHour: Int = 0,
    @Volatile var upMin: Int = 0,
    @Volatile var upSec: Int = 0,
)

data class Loadavg (
    @Volatile var one: Float = 0f,
    @Volatile var oneOccupy: Float = 0f,
    @Volatile var five: Float = 0f,
    @Volatile var fiveOccupy: Float = 0f,
    @Volatile var fifteen: Float = 0f,
    @Volatile var fifteenOccupy: Float = 0f,
    @Volatile var running: Int = 0,
    @Volatile var active: Int = 0,
    @Volatile var lastPid: Int = 0,
)

data class NetDevs (
    @Volatile var total: NetDev = NetDev(),
    @Volatile var devs: MutableList<NetDev> = LinkedList(),
)

data class NetDev (
    @Volatile var name: String = "",
    @Volatile var ips: List<String> = LinkedList(),
    @Volatile var virtual: Boolean = true,
    @Volatile var upBytesH: Float = 0f,
    @Volatile var upBytesHUnit: String = "M",
    @Volatile var upBytes: Long = 0,
    @Volatile var downBytesH: Float = 0f,
    @Volatile var downBytesHUnit: String = "M",
    @Volatile var downBytes: Long = 0,
    @Volatile var upPackets: Long = 0,
    @Volatile var downPackets: Long = 0,
    @Volatile var upSpeed: Float = 0f,
    @Volatile var upSpeedUnit: String = "M",
    @Volatile var downSpeed: Float = 0f,
    @Volatile var downSpeedUnit: String = "M",
)

data class NetStats (
    @Volatile var tcp: NetStatsTCP = NetStatsTCP(),
    @Volatile var udp: NetStatsUDP = NetStatsUDP(),
)

data class NetStatsTCP (
    @Volatile var activeOpens: Int = 0,
    @Volatile var passiveOpens: Int = 0,
    @Volatile var failOpens: Int = 0,
    @Volatile var currConn: Int = 0,
    @Volatile var inSegments: Int = 0,
    @Volatile var outSegments: Int = 0,
    @Volatile var reTransSegments: Int = 0,
    @Volatile var reTransRate: Float = 0f,
)

data class NetStatsUDP (
    @Volatile var inDatagrams: Int = 0,
    @Volatile var outDatagrams: Int = 0,
    @Volatile var receiveBufErrors: Int = 0,
    @Volatile var sendBufErrors: Int = 0,
)

data class Temp (
    @Volatile var map : MutableMap<String, Int> = HashMap()
)

data class Disks (
    @Volatile var total: DiskTotal = DiskTotal(),
    @Volatile var disks: List<Disk> = LinkedList(),
)

data class DiskTotal (
    @Volatile var write: Float = 0f,
    @Volatile var writeUnit: String = "",
    @Volatile var read: Float = 0f,
    @Volatile var readUnit: String = "",
    @Volatile var writeSpeed: Float = 0f,
    @Volatile var writeSpeedUnit: String = "",
    @Volatile var readSpeed: Float = 0f,
    @Volatile var readSpeedUnit: String = "",
)

data class Disk (
    @Volatile var devName: String = "",
    @Volatile var mount: String = "",
    @Volatile var fileSystem: String = "",
    @Volatile var free: Float = 0f,
    @Volatile var freeOccupy: Float = 0f,
    @Volatile var freeUnit: String = "",
    @Volatile var used: Float = 0f,
    @Volatile var usedOccupy: Float = 0f,
    @Volatile var usedUnit: String = "",
    @Volatile var total: Float = 0f,
    @Volatile var totalUnit: String = "",
    @Volatile var write: Float = 0f,
    @Volatile var writeUnit: String = "",
    @Volatile var read: Float = 0f,
    @Volatile var readUnit: String = "",
    @Volatile var writeSpeed: Float = 0f,
    @Volatile var writeSpeedUnit: String = "",
    @Volatile var readSpeed: Float = 0f,
    @Volatile var readSpeedUnit: String = "",
    @Volatile var writeIOPS: Int = 0,
    @Volatile var readIOPS: Int = 0,
)
