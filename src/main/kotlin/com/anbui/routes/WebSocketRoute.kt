package com.anbui.routes

import com.anbui.data.Player
import com.anbui.data.Room
import com.anbui.data.models.messages.*
import com.anbui.server
import com.anbui.session.DrawingSession
import com.anbui.utils.BaseSerializerModule
import com.anbui.utils.ResponseMessages
import com.anbui.utils.send
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

/**
 * This function handle *payload* data sent from player to websocket server.
 * when the *payload* is
 * - [JoinRoomHandshake]:
 * - [DrawData] :
 * - [ChosenWord]:
 *
 */
fun Route.gameWebSocketRoute() {
    standardWebSocket("/ws/draw") { socket, clientId, message, payload ->
        when (payload) {
            is JoinRoomHandshake -> {
                val room = server.rooms[payload.roomName] ?: run {
                    println(payload.roomName)
                    val gameError = GameError(GameError.ERROR_ROOM_NOT_FOUND)
                    socket.send(gameError)
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
                } else {
                    val playerInRoom = room.players.find { it.clientId == clientId }
                    playerInRoom?.socket = socket
                    playerInRoom?.startPing()
                }
            }

            is DrawData -> {
                val room = server.rooms[payload.roomName] ?: return@standardWebSocket
                if (room.phase == Room.Phase.GAME_RUNNING) {
                    room.broadcastToAllExcept(message, clientId)
                    room.addDraw(message)
                }
                room.lastDrawData = payload
            }

            is DrawAction -> {
                val room = server.getRoomWithClientId(clientId) ?: return@standardWebSocket
                room.broadcastToAllExcept(message, clientId)
                room.addDraw(message)
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

            is Ping -> {
                server.players[clientId]?.receivePong()
            }

            is Disconnect -> {
                server.playerLeft(clientId, true)
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

        println(session)

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    val format = BaseSerializerModule.baseJson
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