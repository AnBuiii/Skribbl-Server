package com.anbui.data

import com.anbui.data.models.messages.BaseModel
import com.anbui.data.models.messages.Ping
import com.anbui.server
import com.anbui.utils.send
import io.ktor.websocket.*
import kotlinx.coroutines.*

/**
 * Represent a player of this game
 * @param username unique username
 * @param socket
 * @param clientId uni
 */
data class Player(
    val username: String,
    private var socket: WebSocketSession,
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

    fun setSession(socket: WebSocketSession){
        this.socket = socket;
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startPing() {
        pingJob?.cancel()

        pingJob = GlobalScope.launch {
            while (true) {
                sendPing()
            }
        }
    }

    /**
     *  Send ping and wait for pong, if [pongTime] - [pingTime] > [PING_FREQUENCY], it means the user is disconnected
     */
    private suspend fun sendPing() {
        pingTime = System.currentTimeMillis()

        sendSomething(Ping())
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

    suspend fun sendSomething(baseModel: BaseModel) {
        if (socket.isActive) {
            socket.send(baseModel)
        }
    }

    companion object {
        private const val PING_FREQUENCY = 3000L
    }
}
