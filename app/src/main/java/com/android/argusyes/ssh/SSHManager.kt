package com.android.argusyes.ssh

import android.content.Context
import com.android.argusyes.dao.ServerDao
import com.android.argusyes.dao.entity.Server

class SSHManager private constructor(context: Context){

    private val serverDao = ServerDao.getInstance(context)
    private val sshs: MutableList<SSH> = serverDao.list().map { SSH(it) } as MutableList<SSH>
    private val sshMap: MutableMap<String, SSH> = sshs.associateBy { it.data.id } as MutableMap<String, SSH>

    fun getSSH() : List<SSH> {
        return this.sshs
    }

    fun addServer(server: Server) {
        if (sshMap.containsKey(server.id)) {
            return
        }
        serverDao.save(server)
        val ssh = SSH(server)
        sshs.add(ssh)
        sshMap[ssh.data.id] = ssh
    }

    fun removeServerById(id: String) {
        serverDao.removeById(id)
        val ssh = sshMap.remove(id)
        sshs.remove(ssh)
        ssh?.monitor?.stopMonitor()
    }

    fun updateServerById(server: Server) {
        serverDao.updateById(server)
        val ssh = SSH(server)
        val old = sshMap.put(ssh.data.id, ssh)
        val index = sshs.indexOf(old)
        sshs[index] = ssh
    }

    fun getServerById(id: String) : SSH? {
        return sshMap[id]
    }

    companion object {

        @Volatile private var instance: SSHManager? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: SSHManager(context).also { instance = it }
            }
    }

}