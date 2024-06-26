package com.anbui.data.models.messages

import kotlinx.serialization.Serializable

/**
 * Represent player data send by websocket
 */
@Serializable
data class PlayerData(
    val username: String,
    var isDrawing: Boolean = false,
    var score: Int = 0,
    var rank: Int = 0
)
