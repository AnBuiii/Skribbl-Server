package com.anbui.routes

import com.anbui.data.Player
import com.anbui.data.Room
import com.anbui.data.models.messages.*
import com.anbui.server
import com.anbui.session.DrawingSession
import com.anbui.utils.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * This function handle *payload* data sent from player to websocket server.
 * when the *payload* is
 * - [JoinRoomHandshake]:
 * - [DrawData] :
 * - [ChosenWord]:
 *
 */

@OptIn(DelicateCoroutinesApi::class)
fun Route.gameWebSocketRoute() {
    standardWebSocket("/ws/draw") { socket, clientId, message, payload ->
        when (payload) {
            is JoinRoomHandshake -> {
                val room = server.rooms[payload.roomName] ?: run {
                    val gameError = GameError(GameError.ERROR_ROOM_NOT_FOUND)
                    socket.send(Frame.Text(Json.encodeToString(gameError)))
                    return@standardWebSocket
                }
                val player = Player(
                    payload.username,
                    socket,
                    payload.clientId
                )
                server.playerJoined(player)
                if (!room.containPlayer(player.username)) {
                    room.addPlayer(player.clientId, player.username, socket)
                }
            }

            is DrawData -> {
//                val room = server.rooms[payload.roomName] ?: return@standardWebSocket

//                println(message)
                server.players.forEachEntry(1000L) { (t, it) ->
//                    GlobalScope.launch {
//                        println(t)
//                        it.socket.send(Frame.Text(message))
//                    }
                    if(it.clientId != clientId){
                        GlobalScope.launch {
                            println(t)
                            it.socket.send(Frame.Text(message))
                        }
                    }

                }

//                socket.send(Frame.Text)


//                if (room.phase == Room.Phase.GAME_RUNNING) {
//                    room.broadcastToAllExcept(message, clientId)
//                }
            }

            is ChosenWord -> {
                val room = server.rooms[payload.roomName] ?: return@standardWebSocket
                room.setWordAndSwitchToGameRunning(payload.chosenWord)
            }

            is ChatMessage -> {
                val room = server.rooms[payload.roomName] ?: return@standardWebSocket
                if (!room.checkWordAndNotifyPlayer(payload)) {
                    room.broadcast(message)
                }
            }
        }
    }
}

/**
 * Standard websocket
 * @param path path for ws route
 * @param handleFrame callback for frame handle
 */
fun Route.standardWebSocket(
    path: String,
    handleFrame: suspend (
        socket: DefaultWebSocketSession,
        clientId: String,
        message: String,
        payload: BaseModel
    ) -> Unit
) {
    webSocket(path) {
        // check if session is parcelable
        val session = call.sessions.get<DrawingSession>() ?: run {
            close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    ResponseMessages.NO_SESSION
                )
            )
            return@webSocket
        }

        val player = Player(
            UUID.randomUUID().toString(),
            this,
            session.sessionId
        )
        server.playerJoined(player)

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    val format = Json { serializersModule = baseModelSerializerModule }
                    val payload = format.decodeFromString<BaseModel>(message)
                    handleFrame(this, session.sessionId, message, payload)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            val playerWithClientId = server.getRoomWithClientId(session.clientId)?.players?.find {
                it.clientId == session.clientId
            }
            if (playerWithClientId != null) {
                server.playerLeft(session.clientId)
            }
        }
    }
}