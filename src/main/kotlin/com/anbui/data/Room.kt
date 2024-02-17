package com.anbui.data

import com.anbui.data.models.Announcement
import com.anbui.data.models.PhaseChange
import com.anbui.utils.Constants
import com.anbui.utils.ResponseMessages
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represent the room
 */
class Room(
    val name: String,
    val maxPlayer: Int,
    var players: List<Player> = listOf()
) {

    /**
     * Job for game's countdown timer
     */
    private var timerJob: Job? = null

    /**
     * Current drawing player
     */
    private var drawingPlayer: Player? = null

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
     * Add new player to the room and update the current phase.
     * Players are shuffled in waiting for start phase to make game fair
     */
    suspend fun addPlayer(clientId: String, username: String, socketSession: WebSocketSession): Player {
        val player = Player(username = username, socket = socketSession, clientId = clientId)
        players = players + player

        if (players.size == 1) {
            phase = Phase.WAITING_FOR_PLAYER
        } else if (players.size == Constants.MIN_ROOM_SIZE && phase == Phase.WAITING_FOR_PLAYER) {
            phase = Phase.WAITING_FOR_START
            players = players.shuffled()
        } else if (players.size == Constants.MAX_ROOM_SIZE && phase == Phase.WAITING_FOR_START) {
            phase = Phase.NEW_ROUND
            players = players.shuffled()
        }

        val announcement = Announcement(
            message = "$username ${ResponseMessages.PLAYER_JOIN}",
            System.currentTimeMillis(),
            Announcement.PLAYER_JOINED
        )

        broadcast(Json.encodeToString(announcement))
        return player
    }

    /**
     * function to set timer and notify client once and while with new time to synchronize timer
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun timeAndNotify(ms: Long) {
        timerJob?.cancel()
        timerJob = GlobalScope.launch {
            val phaseChange = PhaseChange(
                phase,
                ms,
                drawingPlayer?.username
            )
            repeat((ms / UPDATE_TIME_FREQUENCY).toInt()) {
                if (it != 0) {
                    phaseChange.phase = null
                }
                broadcast(Json.encodeToString(phaseChange))
                phaseChange.timeStamp -= UPDATE_TIME_FREQUENCY
                delay(UPDATE_TIME_FREQUENCY)
            }

            phase = when (phase) {
                Phase.WAITING_FOR_PLAYER -> Phase.NEW_ROUND
                Phase.NEW_ROUND -> Phase.GAME_RUNNING
                Phase.GAME_RUNNING -> Phase.SHOW_WORD
                Phase.SHOW_WORD -> Phase.NEW_ROUND
                else -> Phase.WAITING_FOR_PLAYER
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
     * Begin phase, notify players there is one player is waiting in room
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun waitingForPlayer() {
        GlobalScope.launch {
            val phaseChange = PhaseChange(
                Phase.WAITING_FOR_PLAYER,
                DELAY_WAITING_FOR_START_NEW_ROUND
            )
            broadcast(Json.encodeToString(phaseChange))
        }
    }

    /**
     * After second player join, start timer to start game
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun waitingForStart() {
        timeAndNotify(DELAY_WAITING_FOR_START_NEW_ROUND)
        GlobalScope.launch {
            val phaseChange = PhaseChange(
                Phase.WAITING_FOR_PLAYER,
                DELAY_WAITING_FOR_START_NEW_ROUND
            )
            broadcast(Json.encodeToString(phaseChange))
        }
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

    companion object {
        const val UPDATE_TIME_FREQUENCY = 1000L
        const val DELAY_WAITING_FOR_START_NEW_ROUND = 10000L
        const val DELAY_NEW_ROUND_TO_GAME_RUNNING = 20000L
        const val DELAY_GAME_RUNNING_TO_SHOW_WORD = 60000L
        const val DELAY_SHOW_WORD_TO_NEW_ROUND = 10000L
    }
}