package com.anbui.data

import com.anbui.data.models.clientMessage.Announcement
import com.anbui.data.models.clientMessage.ChosenWord
import com.anbui.data.models.clientMessage.GameState
import com.anbui.data.models.clientMessage.PhaseChange
import com.anbui.utils.Constants
import com.anbui.utils.ResponseMessages
import com.anbui.utils.transformToUnderscores
import com.anbui.utils.words
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
     * Players who guess it at current row
     */
    private var winningPLayer: List<String> = emptyList()

    /**
     * The word
     */
    private var word: String? = null

    /**
     *
     */
    private var curWords: List<String>? = null

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
     * Add new player to the room and update [phase].
     * [players] are shuffled in [Phase.WAITING_FOR_START] to make game fair
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
     * Function to set timer and notify all player in order to synchronise their timer.
     * Every [UPDATE_TIME_FREQUENCY] millisecond, this function will send broadcast once while player synchronise
     * their timer. After [ms], the next [phase] occur
     * @param ms this [phase] delay
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
     * Send [message] to all player in room if they are active
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
     * Draw player chose a word and [phase] change to [Phase.GAME_RUNNING]
     */
    fun setWordAndSwitchToGameRunning(word: String) {
        this.word = word
        phase = Phase.GAME_RUNNING

    }

    /**
     * Check if room contain this player's [username]
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
     * This function run when [phase] change to [Phase.GAME_RUNNING].
     * Drawing player choose 1 [word] from [curWords], or else 1 word will be got randomly from [static resources][words]).
     * Send message with [transformToUnderscores] word to other player, except the drawing player
     * will be sent the [word]
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun gameRunning() {
        winningPLayer = emptyList()
        val wordToSend = word ?: curWords?.random() ?: words.random()
        val transformedWord = wordToSend.transformToUnderscores()
        val drawingUsername = (drawingPlayer ?: players.random()).username
        val gameStateForDrawingPlayer = GameState(
            drawingUsername,
            wordToSend
        )
        val gameStateForGuessPlayer = GameState(
            drawingUsername,
            transformedWord
        )
        GlobalScope.launch {
            broadcastToAllExcept(
                Json.encodeToString(gameStateForGuessPlayer),
                (drawingPlayer ?: players.random()).clientId
            )
            drawingPlayer?.socket?.send(Frame.Text(Json.encodeToString(gameStateForDrawingPlayer)))
        }
    }

    /**
     *  At the end of game, sent the chosen word to players. If nobody guess it, the draw player get penalty
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun showWord() {
        GlobalScope.launch {
            if (winningPLayer.isEmpty()) {
                drawingPlayer?.let {
                    it.score -= PENALTY_NOBODY_GUESS_IT
                }
            }
            word?.let {
                val chosenWord = ChosenWord
                broadcast(Json.encodeToString(chosenWord))
            }
            timeAndNotify(DELAY_SHOW_WORD_TO_NEW_ROUND)
            val phaseChange = PhaseChange(
                Phase.SHOW_WORD,
                DELAY_WAITING_FOR_START_NEW_ROUND
            )
            broadcast(Json.encodeToString(phaseChange))
        }
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
        const val PENALTY_NOBODY_GUESS_IT = 50
    }
}