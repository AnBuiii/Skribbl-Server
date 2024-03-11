package com.anbui.utils

import com.anbui.data.models.messages.BaseModel
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString

/**
 * Custom socket send base model function
 */
suspend fun WebSocketSession.send(baseModel: BaseModel) {
    val json = BaseSerializerModule.baseJson
    val message = json.encodeToString(baseModel)
    send(Frame.Text(message))
}