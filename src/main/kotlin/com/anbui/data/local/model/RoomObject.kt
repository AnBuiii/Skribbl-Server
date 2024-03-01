package com.anbui.data.local.model

import org.jetbrains.exposed.sql.Table

object RoomObject : Table() {
    val id = integer("id").autoIncrement()
    val roomName = varchar("roomName", 128)
    val maxPlayer = integer("maxPlayer")

    override val primaryKey = PrimaryKey(id)
}