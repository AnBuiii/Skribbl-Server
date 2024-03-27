package com.anbui.data

import com.anbui.data.models.Line

sealed class Command {
    data class DrawCommand(val line: Line) : Command()
    data object UndoCommand : Command()
    data object RedoCommand : Command()
}
