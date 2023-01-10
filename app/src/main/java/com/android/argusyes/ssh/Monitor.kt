package com.android.argusyes.ssh

import com.android.argusyes.dao.entity.Server
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*


enum class ConnectStatus {
    INIT, SUCCESS, FAIL
}

class Monitor (var server: Server){

    var connectStatus = ConnectStatus.INIT
    val monitorInfo: MonitorInfo = MonitorInfo()

    @OptIn(DelicateCoroutinesApi::class)
    private var monitorJob: Job = GlobalScope.launch(Dispatchers.IO) {
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

            while (this.isActive) {
                delay(2000)
                monitorInfo.cpus.total.processor = monitorInfo.cpus.total.processor + 1
            }

            sftp.exit()
            session.disconnect()
        } catch (e : Exception) {
            connectStatus = ConnectStatus.FAIL
        }
    }

    fun stopMonitor() = monitorJob.cancel()


    private suspend fun readFile(fileName : String, sftp: ChannelSftp): String {
        val result = ByteArrayOutputStream();
        sftp.get(fileName, result)
        return result.toString(StandardCharsets.UTF_8.name())
    }
}
