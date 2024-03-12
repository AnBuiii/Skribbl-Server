package com.anbui

import com.anbui.data.Player
import com.anbui.data.Room
import java.util.concurrent.ConcurrentHashMap

/**
 * Represent the game server
 */
class DrawingServer {
    val rooms = ConcurrentHashMap<String, Room>()
    val players = ConcurrentHashMap<String, Player>()

    /**
     * Insert new player
     */
    fun playerJoined(player: Player) {
        players[player.clientId] = player
        player.startPing()
    }

    /**
     * Get the first room has the player with matched clientId
     */
    fun getRoomWithClientId(clientId: String): Room? {
        val filteredRooms = rooms.filterValues { room ->
            room.players.any { player ->
                player.clientId == clientId
            }
        }
        return filteredRooms.values.toList().firstOrNull()
    }

    fun playerLeft(clientId: String, immediatelyDisconnect: Boolean = false) {
        val playerRoom = getRoomWithClientId(clientId)
        if (immediatelyDisconnect || players[clientId]?.isOnline == false) {
            println("${players[clientId]?.username} is disconnected")
            players[clientId]?.disconnect()
            playerRoom?.removePlayer(clientId)
        }
    }
}

fun main() {
    val a = listOf("asd", "", "a", "okok", "", "asd" ,"" ,"" ,"a","")
    println(a.splitBy(true) { it == "" })
}

fun <T> List<T>.splitBy(included: Boolean = false, selector: (T) -> Boolean): List<List<T>> {
    val result = mutableListOf<List<T>>()
    val temp = mutableListOf<T>()
    forEach { item ->
        if (!selector(item)) {
            temp.add(item)
        } else {
            if (included) {
                temp.add(item)
            }
            result.add(temp.toList())
            temp.clear()
        }
    }
    if (temp.isNotEmpty()) result.add(temp)
    return result
}