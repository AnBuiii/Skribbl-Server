package com.anbui.data

import com.anbui.data.models.messages.Ping
import com.anbui.server
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represent a player of this game
 * @param username unique username
 * @param socket
 * @param clientId uni
 */
data class Player(
    val username: String,
    var socket: WebSocketSession,
    val clientId: String,
    var isDrawing: Boolean = false,
    var score: Int = 0,
    val rank: Int = 0
) {
    /**
     * Job to sent ping
     */
    private var pingJob: Job? = null

    /**
     * Represent time of ping sent
     */
    private var pingTime: Long = 0L

    /**
     * Represent time of receive ping respond
     */
    private var pongTime: Long = 0L

    var isOnline = true

    @OptIn(DelicateCoroutinesApi::class)
    fun startPing() {
        pingJob?.cancel()

        pingJob = GlobalScope.launch {
            while (true) {
                sendPing()
                delay(PING_FREQUENCY)
            }
        }
    }

    /**
     *  Send ping and wait for pong, if [pongTime] - [pingTime] > [PING_FREQUENCY], it means the user is disconnected
     */
    private suspend fun sendPing() {
        pingTime = System.currentTimeMillis()

        socket.send(Frame.Text(Json.encodeToString(Ping())))
        delay(PING_FREQUENCY)

        if (pingTime - pongTime > PING_FREQUENCY) {
            isOnline = false
            server.playerLeft(clientId)
            pingJob?.cancel()
        }
    }

    /**
     * Save [pongTime] when receive pong
     */
    fun receivePong() {
        pongTime = System.currentTimeMillis()
        isOnline = true
    }

    /**
     * cancel all alive job
     */
    fun disconnect() {
        pingJob?.cancel()
    }

    companion object {
        private const val PING_FREQUENCY = 3000L
    }
}
