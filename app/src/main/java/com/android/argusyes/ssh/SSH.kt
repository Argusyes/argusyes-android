package com.android.argusyes.ssh

import com.android.argusyes.dao.entity.Server

class SSH(var data: Server) {
    var monitor = Monitor(data)
}