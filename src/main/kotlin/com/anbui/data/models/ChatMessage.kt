package com.anbui.data.models

import com.anbui.utils.ModelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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
 * not use
 */
@Serializable
@SerialName(ModelType.ANOTHER_CHAT_MESSAGE)
data class NotBaseModel(
    val notFrom: String
) : BaseModel()

/**
 * Polymorphic type handler for [BaseModel]
 * To deserialize BaseModel subclass, data from json MUST have type parameter, like { "type" : "ChatMessage" }, mapping
 * to which type it will deserialize to
 */
val baseModelSerializerModule = SerializersModule {
    polymorphic(BaseModel::class) {
        subclass(NotBaseModel::class)
        defaultDeserializer { ChatMessage.serializer() }
    }
}


