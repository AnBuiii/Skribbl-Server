package com.anbui.data.models

import com.anbui.utils.ModelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 *  Standard for websocket's data
 */
@Serializable
abstract class BaseModel

/**
 * not use
 */
@Serializable
@SerialName(ModelType.NOT_BASE_MODEL)
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
@SerialName(ModelType.CHAT_MESSAGE)
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
@SerialName(ModelType.DRAW_DATA)
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


/**
 * Polymorphic type handler for [BaseModel]
 * To deserialize BaseModel subclass, data from json MUST have type parameter, like { "type" : "ChatMessage" }, mapping
 * to which type it will deserialize to
 */
val baseModelSerializerModule = SerializersModule {
    polymorphic(BaseModel::class) {
        subclass(ChatMessage::class)
        subclass(DrawData::class)
        defaultDeserializer { NotBaseModel.serializer() }
    }
}
