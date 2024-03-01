package com.anbui.data.local

import com.anbui.data.Room
import com.anbui.data.local.model.DrawObject
import com.anbui.data.local.model.RoomObject
import com.anbui.data.models.messages.DrawData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DAOFacadeImpl : DAOFacade {
    private fun resultRowToArticle(row: ResultRow) = DrawData(
        roomName = row[DrawObject.roomName],
        color = row[DrawObject.color],
        thickness = row[DrawObject.thickness],
        fromX = row[DrawObject.fromX],
        fromY = row[DrawObject.fromY],
        toX = row[DrawObject.toX],
        toY = row[DrawObject.toY],
        motionEvent = row[DrawObject.motionEvent]
    )

    private fun resultRowToRoom(row: ResultRow) =
        Room(
            name = row[RoomObject.roomName],
            maxPlayer = row[RoomObject.maxPlayer],
            players = listOf()
        )

    override suspend fun allDrawData(): List<DrawData> {
        return DrawObject.selectAll().map(::resultRowToArticle)
    }

    override suspend fun allDrawDataInRoom(roomName: String): List<DrawData> {

        return DrawObject.select { DrawObject.roomName eq roomName }
            .map(::resultRowToArticle)
    }

    override suspend fun newDrawData(drawData: DrawData): DrawData? {
        val insertStatement = DrawObject.insert {
            it[roomName] = drawData.roomName
            it[color] = drawData.color
            it[thickness] = drawData.thickness
            it[fromX] = drawData.fromX
            it[fromX] = drawData.fromY
            it[toX] = drawData.toX
            it[toY] = drawData.toY
            it[motionEvent] = drawData.motionEvent
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToArticle)
    }

    override suspend fun allRoomObject(): List<Room> {
        return RoomObject.selectAll().map(::resultRowToRoom)
    }

    override suspend fun newRoomObject(room: Room): Room? {
        return SkribblDatabase.dbQuery {
            val insertStatement = RoomObject.insert {
                it[roomName] = room.name
                it[maxPlayer] = room.maxPlayer
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToRoom)
        }

    }

    override suspend fun editRoom(id: Int, room: Room): Boolean {
        return SkribblDatabase.dbQuery {
            RoomObject.update(where = { RoomObject.id eq id }) {
                it[roomName] = room.name
                it[maxPlayer] = room.maxPlayer
            } > 0
        }
    }

    override suspend fun deleteRoom(id: Int): Boolean {
        return SkribblDatabase.dbQuery {
            RoomObject.deleteWhere {
                RoomObject.id eq id
            } > 0
        }
    }
}