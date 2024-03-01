package com.anbui.data.local.model

import org.jetbrains.exposed.sql.Table

object DrawObject : Table() {
    val id = integer("id").autoIncrement()
    val roomName = varchar("roomName", 128)

    val color = integer("color")
    val thickness = float("thickness")
    val fromX = float("fromX")
    val fromY = float("fromY")
    val toX = float("toX")
    val toY = float("toY")
    val motionEvent = integer("motionEvent")

    override val primaryKey = PrimaryKey(id)
}