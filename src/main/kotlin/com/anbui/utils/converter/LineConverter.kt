package com.anbui.utils.converter

import com.anbui.data.models.Line
import com.anbui.data.models.messages.LineData

fun LineData.toLine(): Line {
    return Line(
        points = points,
        color = color,
        thickness = thickness,
    )
}

fun Line.toLineData(roomName: String): LineData {
    return LineData(roomName = roomName, points = points, thickness = thickness, color = color)
}