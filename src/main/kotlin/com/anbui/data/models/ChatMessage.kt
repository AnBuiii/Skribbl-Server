package com.anbui.data.models

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
data class ChatMessage(
    val to: String,
) : BaseModel()

@Serializable
@SerialName("NotChat")
data class NotChat(
    val notFrom: String
) : BaseModel()

/**
 * Polymorphic type handler for [BaseModel]
 * To deserialize BaseModel subclass, data from json MUST have type parameter, like { "type" : "ChatMessage" }, mapping
 * to which type it will deserialize to
 */
val baseModelSerializerModule = SerializersModule {
    polymorphic(BaseModel::class) {
        subclass(NotChat::class)
        defaultDeserializer { ChatMessage.serializer() }
    }
}
//
//@Serializable
//abstract class Project {
//    abstract val name: String
//}
//
//@Serializable
//data class BasicProject(override val name: String, val type: String) : Project()
//
//@Serializable
//@SerialName("OwnedProject")
//data class OwnedProject(override val name: String, val owner: String) : Project()

