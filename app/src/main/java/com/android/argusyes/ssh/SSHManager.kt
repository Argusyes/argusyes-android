package com.android.argusyes.ssh

import android.content.Context
import com.android.argusyes.dao.ServerDao
import com.android.argusyes.dao.entity.Server
import java.util.*

class SSHManager private constructor(context: Context){

    private val serverDao = ServerDao.getInstance(context)
    private val servers = LinkedList<Server>(serverDao.list())
    private val serverMap = servers.associateBy { server: Server -> server.id } as MutableMap

    fun getServers() : List<Server> {
        return this.servers
    }

    fun addServer(server: Server) {
        if (serverMap.containsKey(server.id)) {
            return
        }
        serverDao.save(server)
        servers.add(server)
    }

    fun removeServerById(id: String) {
        serverDao.removeById(id)
        servers.remove(serverMap.remove(id))
    }

    fun updateServerById(server: Server) {
        serverDao.updateById(server)
        val old = serverMap.put(server.id, server)
        val index = servers.indexOf(old)
        servers[index] = server
    }

    fun getServerById(id: String) : Server {
        return servers.first { it.id == id }
    }

    companion object {

        @Volatile private var instance: SSHManager? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: SSHManager(context).also { instance = it }
            }
    }

}