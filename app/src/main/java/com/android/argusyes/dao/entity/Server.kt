package com.android.argusyes.dao.entity

data class Server(
    var id:Int?,
    val name:String,
    var host:String,
    var port:Int,
    var userName:String,
    var password:String)
