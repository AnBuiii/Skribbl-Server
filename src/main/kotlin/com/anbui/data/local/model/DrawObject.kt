package com.anbui.data.local.model

import org.jetbrains.exposed.sql.Table

object DrawObject : Table() {
    val id = integer("id").autoIncrement()
    val roomName = varchar("roomName", 128)

    val color = integer("color")
    val thickness = float("thickness")
    val x = float("x")
    val y = float("y")
    val motionEvent = integer("motionEvent")

    override val primaryKey = PrimaryKey(id)
}