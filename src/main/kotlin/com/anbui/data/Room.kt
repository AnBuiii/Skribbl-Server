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
     * listener for phase change
     */
    private var phaseChangeListener: ((Phase) -> Unit)? = null
    var phase = Phase.WAITING_FOR_PLAYER
        private set(value) {
            synchronized(field) {
                field = value
                phaseChangeListener?.let { change ->
                    change(value)
                }
            }
        }

    private fun setPhaseChangeListener(listener: (Phase) -> Unit) {
        phaseChangeListener = listener
    }

    init {
        setPhaseChangeListener { newPhase ->
            when (newPhase) {
                Phase.WAITING_FOR_PLAYER -> {
                    waitingForPlayer()
                }

                Phase.WAITING_FOR_START -> {
                    waitingForStart()
                }

                Phase.NEW_ROUND -> {
                    newRound()
                }

                Phase.GAME_RUNNING -> {
                    gameRunning()
                }

                Phase.SHOW_WORD -> {
                    showWord()
                }
            }
        }
    }

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

    /**
     * Check if room contain this player's username
     */
    fun containPlayer(username: String): Boolean {
        return players.any { player ->
            player.username == username
        }
    }

    /**
     *
     */
    private fun waitingForPlayer() {

    }

    /**
     *
     */
    private fun waitingForStart() {

    }

    /**
     *
     */
    private fun newRound() {

    }

    /**
     *
     */
    private fun gameRunning() {

    }

    /**
     *
     */
    private fun showWord() {

    }

    enum class Phase {
        WAITING_FOR_PLAYER,
        WAITING_FOR_START,
        NEW_ROUND,
        GAME_RUNNING,
        SHOW_WORD
    }


}