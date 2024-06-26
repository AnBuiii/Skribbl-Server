package com.anbui.data.models.messages

import com.anbui.data.Room
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent phase change message sent by SERVER
 * @param phase current
 * @param timeStamp delay time to next phase in milliseconds
 * @param drawingPlayer player who draw
 */
@Serializable
@SerialName(BaseModel.PHASE_CHANGE)
data class PhaseChange(
    var phase: Room.Phase?,
    var timeStamp: Long,
    val drawingPlayer: String? = null
) : BaseModel()
