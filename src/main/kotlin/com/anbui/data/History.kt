package com.anbui.data

typealias Snapshot = Pair<Command, Memento>

class History() {
    private var histories: MutableList<Snapshot> = mutableListOf()
    private var virtualSize: Int = 0


    fun push(command: Command, memento: Memento) {
        if (virtualSize != histories.size - 1) {
            histories = histories.subList(0, virtualSize - 1)
        }
        histories.add(command to memento)
        virtualSize = histories.size
    }

    suspend fun undo(): Boolean {
        if (virtualSize == 0) return false
        virtualSize = maxOf(virtualSize - 1, 0)
        val snapshot = histories[virtualSize]
        snapshot.second.restore()
        return true
    }

    suspend fun redo(): Boolean {
        if (virtualSize == histories.size - 1) return false
        virtualSize = minOf(virtualSize + 1, histories.size - 1)
        val snapshot = histories[virtualSize]
        snapshot.second.restore()
        return true
    }
}