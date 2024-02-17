package com.anbui.data.models

import com.anbui.data.Room
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent phase change message sent by client
 * @param phase new phase
 * @param timeStamp current time in milliseconds
 * @param drawingPlayer player that cause phase change
 */
@Serializable
@SerialName(BaseModel.PHASE_CHANGE)
data class PhaseChange(
    var phase: Room.Phase?,
    var timeStamp: Long,
    val drawingPlayer: String? = null
): BaseModel()
