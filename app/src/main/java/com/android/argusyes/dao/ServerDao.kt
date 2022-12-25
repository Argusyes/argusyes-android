package com.android.argusyes.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.argusyes.dao.entity.Server
import com.android.argusyes.dao.helper.DBHelper
import java.util.LinkedList


class ServerDao private constructor(context: Context){

    private val db: SQLiteDatabase = DBHelper.getInstance(context).writableDatabase

    private fun buildContentValues(server: Server): ContentValues {
        val values = ContentValues()
        values.put(COLUMN_NAME_ID, server.id)
        values.put(COLUMN_NAME_NAME, server.name)
        values.put(COLUMN_NAME_HOST, server.host)
        values.put(COLUMN_NAME_PORT, server.port)
        values.put(COLUMN_NAME_USERNAME, server.userName)
        values.put(COLUMN_NAME_PASSWORD, server.password)
        return values
    }

    fun save(server: Server) {
        db.insert(TABLE_NAME, null, buildContentValues(server))
    }

    fun list(): List<Server> {
        val cursor = db.query(TABLE_NAME,
            listOf(COLUMN_NAME_ID, COLUMN_NAME_NAME, COLUMN_NAME_HOST, COLUMN_NAME_PORT,
                COLUMN_NAME_USERNAME, COLUMN_NAME_PASSWORD).toTypedArray(),
            null, null, null, null, null)

        val res = LinkedList<Server>()
        while (cursor.moveToNext()) {
            res.add(
                Server(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                cursor.getInt(3), cursor.getString(4), cursor.getString(5))
            )
        }
        cursor.close()
        return res
    }

    fun removeById(id: String) {
        db.delete(TABLE_NAME, "$COLUMN_NAME_ID=?", listOf(id).toTypedArray())
    }

    fun updateById(server: Server) {
        db.update(TABLE_NAME, buildContentValues(server), "$COLUMN_NAME_ID=?", listOf(server.id).toTypedArray())
    }

    companion object {

        @Volatile private var instance: ServerDao? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: ServerDao(context).also { instance = it }
            }

        const val TABLE_NAME = "SERVER"
        const val COLUMN_NAME_ID = "ID"
        const val COLUMN_NAME_NAME = "NAME"
        const val COLUMN_NAME_HOST = "HOST"
        const val COLUMN_NAME_PORT = "PORT"
        const val COLUMN_NAME_USERNAME = "USERNAME"
        const val COLUMN_NAME_PASSWORD = "PASSWORD"

        val serverCreateSQL = """
            create table ${TABLE_NAME}(
                $COLUMN_NAME_ID varchar,
                $COLUMN_NAME_NAME varchar,
                $COLUMN_NAME_HOST varchar,
                $COLUMN_NAME_PORT int,
                $COLUMN_NAME_USERNAME varchar,
                $COLUMN_NAME_PASSWORD varchar
            )
        """.trimIndent()
    }
}