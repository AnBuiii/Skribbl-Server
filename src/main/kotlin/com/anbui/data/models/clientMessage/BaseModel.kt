package com.anbui.data.models.clientMessage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  Standard for websocket's data
 */
@Serializable
abstract class BaseModel {
    /**
     * SerialName for Kotlin serialization
     */
    companion object {
        const val NOT_BASE_MODEL = "NOT_BASE_MODEL"

        const val CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
        const val DRAW_DATA = "TYPE_DRAW_DATA"
        const val ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
        const val JOIN_ROOM_HANDSHAKE = "TYPE_JOIN_ROOM_HANDSHAKE"
        const val GAME_ERROR = "TYPE_GAME_ERROR"
        const val PHASE_CHANGE = "TYPE_PHASE_CHANGE"
        const val CHOSEN_WORD = "TYPE_CHOSEN_WORD"
        const val GAME_STATE = "TYPE_GAME_STATE"
    }
}

/**
 * This class is NOT IN USE
 */
@Serializable
@SerialName(BaseModel.NOT_BASE_MODEL)
data class NotBaseModel(
    val notFrom: String
) : BaseModel()


