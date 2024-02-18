package com.anbui.data

import com.anbui.data.models.messages.*
import com.anbui.server
import com.anbui.utils.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

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
     * Username of all players who guess it at current row
     */
    private var winningPLayers: List<String> = emptyList()

    /**
     * The word player have to guess
     */
    private var word: String? = null

    /**
     * Random word from [words] that [drawingPlayer] have to pick one and it will become [word]
     */
    private var curWords: List<String>? = null

    /**
     * Index of current [drawingPlayer] in [players]
     */
    private var drawPlayerIndex: Int = 0

    /**
     * Start time for phase change
     */
    private var startTime = 0L

    /**
     * Save [clientId][Player.clientId] of player who disconnect map with remove [Job].
     * The [job] will delay some time
     *
     * Sample: After a player disconnect for 30sec, that player will be removed from this room.
     */
    private val playerRemoveJobs = ConcurrentHashMap<String, Job>()

    /**
     * Save all players had leaved with their index
     */
    private val leftPlayer = ConcurrentHashMap<String, PlayerWithIndex>()

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

        sendWordToPlayer(player)
        broadcastPlayersState()
        broadcast(Json.encodeToString(announcement))
        return player
    }

    /**
     * remove player from [players]
     * @param clientId
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun removePlayer(clientId: String) {
        val player = players.find { it.clientId == clientId } ?: return
        val index = players.indexOf(player)

        leftPlayer[clientId] = player to index
        players = players - player

        playerRemoveJobs[clientId] = GlobalScope.launch {
            delay(PLAYER_REMOVE_TIME)
            leftPlayer.remove(clientId)
            val playerToRemove = leftPlayer[clientId]

            playerToRemove?.let {
                players = players - it.first
            }

            playerRemoveJobs.remove(clientId)
        }
        val announcement = Announcement(
            "${player.username} ${ResponseMessages.PLAYER_LEFT}",
            System.currentTimeMillis(),
            Announcement.PLAYER_LEFT
        )

        GlobalScope.launch {
            broadcastPlayersState()
            broadcast(Json.encodeToString(announcement))

            if (players.size == 1) {
                phase = Phase.WAITING_FOR_PLAYER
                timerJob?.cancel()
            } else if (players.isEmpty()) {
                kill()
                server.rooms.remove(name)
            }
        }
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
            startTime = System.currentTimeMillis()
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
     * Check if [ChatMessage] sent by player match with [word] in [Phase.GAME_RUNNING].
     * Guess from [winningPLayers] and [drawingPlayer] always false
     */
    private fun isGuessCorrect(guess: ChatMessage): Boolean {
        return guess.isMatchesWord(word ?: return false)
                && !winningPLayers.contains(guess.from)
                && drawingPlayer?.username != guess.from
                && phase == Phase.GAME_RUNNING
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
     * At [Phase.NEW_ROUND], new [drawingPlayer] is picked and [GUESS_WORD_SIZE] words are sent to that player.
     * That player has [DELAY_WAITING_FOR_START_NEW_ROUND] ms to pick [word]
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun newRound() {
        curWords = getRandomWord(GUESS_WORD_SIZE).also { randomWords ->
            val newWords = NewWords(randomWords)
            nextDrawingPlayer()
            GlobalScope.launch {
                drawingPlayer?.socket?.send(Frame.Text(Json.encodeToString(newWords)))
                timeAndNotify(DELAY_NEW_ROUND_TO_GAME_RUNNING)
            }
        }
    }


    /**
     * This function run when [phase] change to [Phase.GAME_RUNNING].
     * Drawing player choose 1 [word] from [curWords], or else 1 word will be got randomly from [static resources][words]).
     * Send message with [transformToUnderscores] word to other player, except the drawing player
     * will be sent the [word]
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun gameRunning() {
        winningPLayers = emptyList()
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
            if (winningPLayers.isEmpty()) {
                drawingPlayer?.let {
                    it.score -= PENALTY_NOBODY_GUESS_IT
                }
            }
            broadcastPlayersState()
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

    /**
     *
     */
    private fun addWiningPlayer(username: String): Boolean {
        winningPLayers = winningPLayers + username
        if (winningPLayers.size == players.size - 1) {
            phase = Phase.NEW_ROUND
            return true
        }
        return false
    }

    /**
     * If some player guess [word] successfully, that player is added to [winningPLayers] and receive some point.
     * [drawingPlayer] also receive some point.
     * An [Announcement] is [broadcast] to all player. A round over announcement also be sent if everyone guess it.
     */
    suspend fun checkWordAndNotifyPlayer(message: ChatMessage): Boolean {
        if (isGuessCorrect(message)) {
            val currentTime = System.currentTimeMillis()
            val guessingTime = currentTime - startTime
            val timePercentageLeft = 1f - guessingTime.toFloat() / DELAY_GAME_RUNNING_TO_SHOW_WORD

            val score = GUESS_SCORE + GUESS_SCORE_PERCENTAGE_MULTIPLIER * timePercentageLeft
            val player = players.find { it.username == message.from }

            player?.let {
                it.score += score.toInt()
            }
            drawingPlayer?.let {
                it.score += GUESS_SCORE_FOR_DRAWING_PLAYER / players.size
            }

            broadcastPlayersState()

            val announcement = Announcement(
                message = "${message.from} ${ResponseMessages.GUESS_IT}",
                timeStamp = currentTime,
                announcementType = Announcement.PLAYER_GUEST_WORD
            )

            broadcast(Json.encodeToString(announcement))

            val isRoundOver = addWiningPlayer(message.from)

            if (isRoundOver) {
                val roundOverAnnouncement = Announcement(
                    message = ResponseMessages.EVERY_ONE_GUESS_IT,
                    timeStamp = System.currentTimeMillis(),
                    announcementType = Announcement.EVERY_BODY_GUESS_IT
                )
                broadcast(Json.encodeToString(roundOverAnnouncement))
            }
        }
        return false
    }

    /**
     * broadcast all players current state sort by their score
     */
    private suspend fun broadcastPlayersState() {
        val playerList = players
            .sortedByDescending(Player::score)
            .mapIndexed { idx, player ->
                PlayerData(
                    username = player.username,
                    isDrawing = player.isDrawing,
                    score = player.score,
                    rank = idx + 1
                )
            }
        broadcast(Json.encodeToString(playerList))
    }

    /**
     * Notify a player join room about current game state.
     * No matter that player join on which [phase]
     */
    suspend fun sendWordToPlayer(player: Player) {
        val delay = when (phase) {
            Phase.WAITING_FOR_START -> DELAY_WAITING_FOR_START_NEW_ROUND
            Phase.NEW_ROUND -> DELAY_NEW_ROUND_TO_GAME_RUNNING
            Phase.GAME_RUNNING -> DELAY_GAME_RUNNING_TO_SHOW_WORD
            Phase.SHOW_WORD -> DELAY_SHOW_WORD_TO_NEW_ROUND
            else -> 0L
        }
        val phaseChange = PhaseChange(phase, delay, drawingPlayer?.username)
        word?.let { w ->
            drawingPlayer?.let { p ->
                val gameState = GameState(
                    p.username,
                    if (p.isDrawing || phase == Phase.SHOW_WORD) {
                        w
                    } else {
                        w.transformToUnderscores()
                    }
                )

                player.socket.send(Frame.Text(Json.encodeToString(gameState)))
            }
        }
        player.socket.send(Frame.Text(Json.encodeToString(Json.encodeToString(phaseChange))))

    }

    private fun nextDrawingPlayer() {
        drawingPlayer?.isDrawing = false

        if (players.isEmpty()) return

        drawingPlayer = if (drawPlayerIndex <= players.size - 1) {
            players[drawPlayerIndex]
        } else players.last()

        if (drawPlayerIndex < players.size - 1) drawPlayerIndex++
        else drawPlayerIndex = 0
    }

    /**
     * Call this function when room need to be destroyed.
     * Kill all job remaining in [playerRemoveJobs] and [timerJob]
     */
    private fun kill() {
        playerRemoveJobs.values.forEach { it.cancel() }
        timerJob?.cancel()
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
        const val GUESS_WORD_SIZE = 3

        const val GUESS_SCORE = 50

        /**
         * If a player guess the word with less than [GUESS_SCORE_PERCENTAGE_MULTIPLIER]% time, they receive more score
         */
        const val GUESS_SCORE_PERCENTAGE_MULTIPLIER = 50

        const val GUESS_SCORE_FOR_DRAWING_PLAYER = 50

        const val PLAYER_REMOVE_TIME = 60000L
    }
}

typealias PlayerWithIndex = Pair<Player, Int>