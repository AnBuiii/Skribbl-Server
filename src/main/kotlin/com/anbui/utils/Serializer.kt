package com.anbui.utils

import com.anbui.data.models.messages.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Polymorphic type handler for [BaseModel]
 * To deserialize BaseModel subclass, data from json MUST have type parameter, like { "type" : "ChatMessage" }, mapping
 * to which type it will deserialize to
 */
val baseModelSerializerModule = SerializersModule {
    polymorphic(BaseModel::class) {
        subclass(ChatMessage::class)
        subclass(DrawData::class)
        subclass(Announcement::class)
        subclass(JoinRoomHandshake::class)
        subclass(GameError::class)
        subclass(PhaseChange::class)
        subclass(ChosenWord::class)
        subclass(GameState::class)
        subclass(NewWords::class)
        defaultDeserializer { NotBaseModel.serializer() }
    }
}

val module = SerializersModule {
    polymorphic(Project::class) {
        subclass(OwnedProject::class)
    }
}

@Serializable
abstract class Project {
    abstract val name: String
}

@Serializable
@SerialName("owned")
class OwnedProject(override val name: String, val owner: String) : Project()

