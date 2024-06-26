package com.anbui.data.models.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent join room message send by client
 */
@Serializable
@SerialName(BaseModel.JOIN_ROOM_HANDSHAKE)
data class JoinRoomHandshake(
    val username: String,
    val clientId: String,
    val roomName: String
) : BaseModel()
