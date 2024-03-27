package com.anbui.data.models.messages

import com.anbui.data.models.Point
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(BaseModel.LINE_DATA)
data class LineData(
    val roomName: String,
    val points: List<Point>,
    val thickness: Int,
    val color: Int
) : BaseModel()