package com.anbui.data.models

import kotlinx.serialization.Serializable

@Serializable
data class  CreateRoomRequest(
    val name: String,
    val maxPlayer: Int
)
