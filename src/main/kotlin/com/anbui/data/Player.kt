package com.anbui.data

import io.ktor.websocket.*

/**
 * Represent a player of this game
 * @param username unique username
 * @param socket
 * @param clientId uni
 */
data class Player(
    val username: String,
    val socket: WebSocketSession,
    val clientId: String,
    var isDrawing: Boolean = false,
    var score: Int = 0,
    val rank: Int = 0
)
