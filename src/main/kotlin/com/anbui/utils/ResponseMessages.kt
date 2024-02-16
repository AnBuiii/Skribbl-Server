package com.anbui.utils

object ResponseMessages {
    const val ROOM_NAME_EXIST = "Room name already exists"
    const val VIOLATE_MIN_ROOM_SIZE = "The minimum room size is ${Constants.MIN_ROOM_SIZE}"
    const val VIOLATE_MAX_ROOM_SIZE = "The maximum room size is ${Constants.MAX_ROOM_SIZE}"
    const val ROOM_NOT_FOUND = "Room not found"
    const val PLAYER_ALREADY_JOINED = "A user with this username already joined"
    const val ROOM_FULL = "This room already full"
    const val NO_SESSION = "No session"
}