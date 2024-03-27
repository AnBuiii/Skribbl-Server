package com.anbui.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Line(
    val points: List<Point>,
    val color: Int,
    val thickness: Int
) {
    companion object {
        const val BLACK = 0
        const val BLUE = 1
        const val RED = 2
        const val GREEN = 3
        const val YELLOW = 4
        const val GRAY = 5
    }
}