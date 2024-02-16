package com.anbui.routes

import com.anbui.data.models.BaseModel
import com.anbui.data.models.baseModelSerializerModule
import com.anbui.session.DrawingSession
import com.anbui.utils.ResponseMessages
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

/**
 * @param handleFrame callback for frame handle
 */
fun Route.standardWebSocket(
    handleFrame: suspend (
        socket: DefaultWebSocketSession,
        clientId: String,
        message: String,
        payload: BaseModel
    ) -> Unit
) {
    webSocket {

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

//                    val typeString = Json.encodeToJsonElement(message).jsonObject["type"].toString()

                    val format = Json { serializersModule = baseModelSerializerModule }

                    val a = format.decodeFromString<BaseModel>(message)

                    println(a)
//                    when (a) {
//                        is ChatMessage -> {
//                            println("1 $a")
//
//                        }
//
//                        is NotChat -> {
//                            println(a)
//
//                        }
//                        else -> {
//                            println("asdasd")
//                        }
//                    }


//                    val a = Json.decodeFromString<Map<String, String>>(message)
//                    val type = when (a["type"]) {
//                        ModelType.CHAT_MESSAGE -> ChatMessage
//                        else -> BaseModel
//                    }
//                    val payload = Json.decodeFromString<String>(message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Handle disconnect
        }


    }
}