package com.anbui.routes

import com.anbui.data.Player
import com.anbui.data.Room
import com.anbui.data.models.*
import com.anbui.server
import com.anbui.session.DrawingSession
import com.anbui.utils.ResponseMessages
import com.anbui.utils.baseModelSerializerModule
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * input for game
 */
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
                val room = server.rooms[payload.roomName] ?: return@standardWebSocket
                if (room.phase == Room.Phase.GAME_RUNNING) {
                    room.broadcastToAllExcept(message, clientId)
                }
                println(payload)
            }

            is ChatMessage -> {

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
            // Handle disconnect
        }
    }
}