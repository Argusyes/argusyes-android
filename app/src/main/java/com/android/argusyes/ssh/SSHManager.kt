package com.android.argusyes.ssh

import android.content.Context
import com.android.argusyes.dao.ServerDao
import com.android.argusyes.dao.entity.Server
import java.util.LinkedList

class SSHManager private constructor(context: Context){

    private val serverDao = ServerDao.getInstance(context)
    private val servers = LinkedList<Server>(serverDao.list())

    fun getServers() : List<Server> {
        return this.servers
    }

    fun addServer(server: Server) {
        serverDao.save(server)
        servers.add(server)
    }

    fun removeServerById(id: String) {
        serverDao.removeById(id)
        servers.remove(servers.first { it.id == id })
    }

    companion object {

        @Volatile private var instance: SSHManager? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: SSHManager(context).also { instance = it }
            }
    }

}