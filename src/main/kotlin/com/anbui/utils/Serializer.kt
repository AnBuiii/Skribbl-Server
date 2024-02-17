package com.anbui.utils

import com.anbui.data.models.clientMessage.*
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
        defaultDeserializer { NotBaseModel.serializer() }
    }
}