package com.anbui.data

class Memento(
    private val room: Room,
) {
    private val backUp: String = room.createBackup()

    suspend fun restore() {
        room.loadBackup(backUp)
    }

}