package com.android.argusyes.ssh

import com.android.argusyes.dao.entity.Server
import kotlinx.coroutines.*


class Monitor (var server: Server){

    val monitorInfo: MonitorInfo = MonitorInfo()

    @OptIn(DelicateCoroutinesApi::class)
    var job: Job = GlobalScope.launch(Dispatchers.IO) {
        while (this.isActive) {
            delay(2000)
            monitorInfo.cpus.total.processor = monitorInfo.cpus.total.processor + 1
        }
    }

    fun startMonitor() = job.start()

}
