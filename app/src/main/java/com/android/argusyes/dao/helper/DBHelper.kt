package com.android.argusyes.dao.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.android.argusyes.dao.ServerDao

class DBHelper private constructor(context: Context, name: String, factory: CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, name, factory, version) {

    private constructor(context: Context) : this(context, "Argusyes.db", null, 1)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ServerDao.serverCreateSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    companion object {

        @Volatile private var instance: DBHelper? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: DBHelper(context).also { instance = it }
            }

    }
}