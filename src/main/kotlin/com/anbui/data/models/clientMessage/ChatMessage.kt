package com.anbui.data.models.clientMessage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * represent chat data
 * @param from id of user who sent this message
 * @param roomName name of room this message sent
 * @param message
 * @param timeStamp when this message was sent
 */
@Serializable
@SerialName(BaseModel.CHAT_MESSAGE)
data class ChatMessage(
    val from: String,
    val roomName: String,
    val message: String,
    val timeStamp: Long
) : BaseModel()