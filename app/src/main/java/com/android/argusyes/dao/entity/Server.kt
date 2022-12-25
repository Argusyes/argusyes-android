package com.android.argusyes.dao.entity

import java.util.UUID

data class Server(
    var id:String = UUID.randomUUID().toString(),
    val name:String,
    var host:String,
    var port:Int,
    var userName:String,
    var password:String,
)
