package com.anbui.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    val name: String,
    val maxPlayer: Int,
    val playerCount: Int
)
