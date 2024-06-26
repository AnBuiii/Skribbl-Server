package com.anbui.utils

import com.anbui.data.models.messages.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Polymorphic type handler for [BaseModel]
 * To deserialize BaseModel subclass, data from json MUST have type parameter, like { "type" : "ChatMessage" }, mapping
 * to which type it will deserialize to
 */


object BaseSerializerModule {
    private val baseModelSerializerModule = SerializersModule {
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
            subclass(Ping::class)
            subclass(Disconnect::class)
            subclass(DrawAction::class)
            subclass(PlayerList::class)
            defaultDeserializer { NotBaseModel.serializer() }
        }
    }

    val baseJson = Json { serializersModule = baseModelSerializerModule }
}