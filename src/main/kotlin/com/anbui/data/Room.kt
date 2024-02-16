package com.anbui.data

import io.ktor.websocket.*
import kotlinx.coroutines.isActive

/**
 * Represent the room
 */
class Room(
    val name: String,
    val maxPlayer: Int,
    var players: List<Player> = listOf()
) {
    /**
     * Send message to all player in room if they are active
     */
    suspend fun broadcast(message: String) {
        players.forEach { player ->
            if (player.socket.isActive) {
                player.socket.send(Frame.Text(message))
            }
        }
    }

    suspend fun broadcastToAllExcept(message: String, clientId: String) {
        players.forEach { player ->
            if (player.socket.isActive && player.clientId != clientId) {
                player.socket.send(Frame.Text(message))
            }
        }
    }
}