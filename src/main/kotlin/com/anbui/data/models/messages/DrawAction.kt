package com.anbui.data.models.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(BaseModel.DRAW_ACTION)
data class DrawAction(
    val action: Int
) : BaseModel() {
    companion object {
        const val ACTION_CLEAR = 0

        const val ACTION_UNDO = 1

        const val ACTION_REDO = 2
    }
}
