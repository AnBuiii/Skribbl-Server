package com.anbui.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  Standard for websocket's data
 */
@Serializable
abstract class BaseModel {
    companion object {
        /**
         * SerialName for Kotlin serialization
         */
        const val NOT_BASE_MODEL = "NOT_BASE_MODEL"

        const val CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
        const val DRAW_DATA = "TYPE_DRAW_DATA"
        const val ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
        const val JOIN_ROOM_HANDSHAKE = "TYPE_JOIN_ROOM_HANDSHAKE"
        const val GAME_ERROR = "TYPE_GAME_ERROR"
    }
}

/**
 * not use
 */
@Serializable
@SerialName(BaseModel.NOT_BASE_MODEL)
data class NotBaseModel(
    val notFrom: String
) : BaseModel()

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

/**
 * Represent draw data, sent from websocket
 *
 * @param roomName name of current room
 * @param color color of current pen
 * @param thickness thickness of current pen
 * @param motionEvent , sample: up, down
 *
 *
 * [fromX], [fromY], [toX], [toY] draw location
 *
 */
@Serializable
@SerialName(BaseModel.DRAW_DATA)
data class DrawData(
    val roomName: String,
    val color: Int,
    val thickness: Float,
    val fromX: Float,
    val fromY: Float,
    val toX: Float,
    val toY: Float,
    val motionEvent: Int
) : BaseModel()


