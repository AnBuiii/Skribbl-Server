package com.anbui.data.models.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.sound.sampled.Line

@Serializable
@SerialName(BaseModel.DRAW_STATE)
data class DrawState(
    val data: List<LineData>
) : BaseModel()