package com.android.argusyes.ssh

data class MonitorInfo (
    @Volatile var cpus: Cpus = Cpus(),
)

data class Cpus(
    @Volatile var map: Map<Int, Cpu> = HashMap(),
    @Volatile var total: Cpu = Cpu(),
    @Volatile var totalTime: Int = 0,
    @Volatile var TotalTimeUnit: String = "",

    )

data class Cpu(
    @Volatile var processor: Int = 0,
    @Volatile var utilization: Double = 0.0,
    @Volatile var free: Double = 0.0,
    @Volatile var system: Double = 0.0,
    @Volatile var user: Double = 0.0,
    @Volatile var IO: Double = 0.0,
    @Volatile var steal: Double = 0.0,
)

