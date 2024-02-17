package com.anbui.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * *Information*
 */
@Serializable
@SerialName(BaseModel.JOIN_ROOM_HANDSHAKE)
data class JoinRoomHandshake(
    val username: String,
    val clientId: String,
    val roomName: String
) : BaseModel()
