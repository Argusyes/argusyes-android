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
    @Volatile var map: MutableMap<Int, Cpu> = HashMap(),
    @Volatile var total: Cpu = Cpu(),
)

data class Cpu(
    @Volatile var processor: Int = 0,
    @Volatile var utilization: Double = 0.0,
    @Volatile var free: Double = 0.0,
    @Volatile var system: Double = 0.0,
    @Volatile var user: Double = 0.0,
    @Volatile var nice: Double = 0.0,
    @Volatile var ioWait: Double = 0.0,
    @Volatile var steal: Double = 0.0,
)

data class Mem(
    @Volatile var total: Double = 0.0,
    @Volatile var totalUnit: String = "",
    @Volatile var free: Double = 0.0,
    @Volatile var freeOccupy: Double = 0.0,
    @Volatile var freeUnit: String = "",
    @Volatile var available: Double = 0.0,
    @Volatile var availableOccupy: Double = 0.0,
    @Volatile var availableUnit: String = "",
    @Volatile var used: Double = 0.0,
    @Volatile var usedOccupy: Double = 0.0,
    @Volatile var usedUnit: String = "",
    @Volatile var buffer: Double = 0.0,
    @Volatile var bufferOccupy: Double = 0.0,
    @Volatile var bufferUnit: String = "",
    @Volatile var cache: Double = 0.0,
    @Volatile var cacheOccupy: Double = 0.0,
    @Volatile var cacheUnit: String = "",
    @Volatile var dirty: Double = 0.0,
    @Volatile var dirtyOccupy: Double = 0.0,
    @Volatile var dirtyUnit: String = "",
    @Volatile var totalSwap: Double = 0.0,
    @Volatile var totalSwapUnit: String = "",
    @Volatile var freeSwap: Double = 0.0,
    @Volatile var freeSwapOccupy: Double = 0.0,
    @Volatile var freeSwapUnit: String = "",
    @Volatile var cachedSwap: Double = 0.0,
    @Volatile var cachedSwapOccupy: Double = 0.0,
    @Volatile var cachedSwapUnit: String = "",
)

data class Uptime(
    @Volatile var upDay: Int = 0,
    @Volatile var upHour: Int = 0,
    @Volatile var upMin: Int = 0,
    @Volatile var upSec: Int = 0,
)

data class Loadavg (
    @Volatile var one: Double = 0.0,
    @Volatile var oneOccupy: Double = 0.0,
    @Volatile var five: Double = 0.0,
    @Volatile var fiveOccupy: Double = 0.0,
    @Volatile var fifteen: Double = 0.0,
    @Volatile var fifteenOccupy: Double = 0.0,
    @Volatile var running: Int = 0,
    @Volatile var active: Int = 0,
    @Volatile var lastPid: Int = 0,
)

data class NetDevs (
    @Volatile var total: NetDev = NetDev(),
    @Volatile var devs: List<NetDev> = LinkedList(),
)

data class NetDev (
    @Volatile var name: String = "",
    @Volatile var ips: List<String> = LinkedList(),
    @Volatile var virtual: Boolean = true,
    @Volatile var upBytesH: Double = 0.0,
    @Volatile var upBytesHUnit: String = "",
    @Volatile var upBytes: Int = 0,
    @Volatile var downBytesH: Double = 0.0,
    @Volatile var downBytesHUnit: String = "",
    @Volatile var downBytes: Int = 0,
    @Volatile var upPackets: Int = 0,
    @Volatile var downPackets: Int = 0,
    @Volatile var upSpeed: Double = 0.0,
    @Volatile var upSpeedUnit: String = "",
    @Volatile var downSpeed: Double = 0.0,
    @Volatile var downSpeedUnit: String = "",
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
    @Volatile var reTransRate: Double = 0.0,
)

data class NetStatsUDP (
    @Volatile var inDatagrams: Int = 0,
    @Volatile var outDatagrams: Int = 0,
    @Volatile var receiveBufErrors: Int = 0,
    @Volatile var sendBufErrors: Int = 0,
)

data class Temp (
    @Volatile var map : Map<String, Int> = HashMap()
)

data class Disks (
    @Volatile var total: DiskTotal = DiskTotal(),
    @Volatile var disks: List<Disk> = LinkedList(),
)

data class DiskTotal (
    @Volatile var write: Double = 0.0,
    @Volatile var writeUnit: String = "",
    @Volatile var read: Double = 0.0,
    @Volatile var readUnit: String = "",
    @Volatile var writeSpeed: Double = 0.0,
    @Volatile var writeSpeedUnit: String = "",
    @Volatile var readSpeed: Double = 0.0,
    @Volatile var readSpeedUnit: String = "",
)

data class Disk (
    @Volatile var devName: String = "",
    @Volatile var mount: String = "",
    @Volatile var fileSystem: String = "",
    @Volatile var free: Double = 0.0,
    @Volatile var freeOccupy: Double = 0.0,
    @Volatile var freeUnit: String = "",
    @Volatile var used: Double = 0.0,
    @Volatile var usedOccupy: Double = 0.0,
    @Volatile var usedUnit: String = "",
    @Volatile var total: Double = 0.0,
    @Volatile var totalUnit: String = "",
    @Volatile var write: Double = 0.0,
    @Volatile var writeUnit: String = "",
    @Volatile var read: Double = 0.0,
    @Volatile var readUnit: String = "",
    @Volatile var writeSpeed: Double = 0.0,
    @Volatile var writeSpeedUnit: String = "",
    @Volatile var readSpeed: Double = 0.0,
    @Volatile var writeIOPS: String = "",
    @Volatile var readIOPS: Int = 0,
)
