package com.anbui

import com.anbui.data.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * Represent the game server
 */
class DrawingServer {
    private val rooms = ConcurrentHashMap<String, Room>()
    private val players = ConcurrentHashMap<String, Player>()

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
}