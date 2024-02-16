package com.anbui.data.models

import kotlinx.serialization.Serializable

/**
 * Response data for search room request
 */
@Serializable
data class RoomResponse(
    val name: String,
    val maxPlayer: Int,
    val playerCount: Int
)
