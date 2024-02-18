package com.anbui

import com.anbui.data.Player
import com.anbui.data.Room
import java.util.concurrent.ConcurrentHashMap

/**
 * Represent the game server
 */
class DrawingServer {
    val rooms = ConcurrentHashMap<String, Room>()
    val players = ConcurrentHashMap<String, Player>()

    /**
     * Insert new player
     */
    fun playerJoined(player: Player) {
        players[player.clientId] = player
    }

    /**
     * Get the first room has the player with matched clientId
     */
    fun getRoomWithClientId(clientId: String): Room? {
        val filteredRooms = rooms.filterValues { room ->
            room.players.any { player ->
                player.clientId == clientId
            }
        }
        return filteredRooms.values.toList().firstOrNull()
    }

    fun playerLeft(clientId: String, immediatelyDisconnect: Boolean = false) {
        val playerRoom = getRoomWithClientId(clientId)
        if (immediatelyDisconnect) {
            println("${players[clientId]?.username} is disconnected")
            playerRoom?.removePlayer(clientId)
        }
    }
}