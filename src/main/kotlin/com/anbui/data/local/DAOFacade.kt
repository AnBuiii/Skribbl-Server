package com.anbui.data.local

import com.anbui.data.Room
import com.anbui.data.models.messages.DrawData

/**
 * A facade to hide complexity of database implement
 */
interface DAOFacade {
    suspend fun allDrawData(): List<DrawData>

    suspend fun allDrawDataInRoom(roomName: String): List<DrawData>

    /**
     * draw object from draw data
     */
    suspend fun newDrawData(drawData: DrawData): DrawData?

    suspend fun allRoomObject(): List<Room>

    suspend fun newRoomObject(room: Room): Room?

    suspend fun editRoom(id: Int, room: Room): Boolean

    suspend fun deleteRoom(id: Int): Boolean


}

