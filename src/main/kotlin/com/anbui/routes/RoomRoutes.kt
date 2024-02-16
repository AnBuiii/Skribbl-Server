package com.anbui.routes

import com.anbui.data.Room
import com.anbui.data.models.BasicApiResponse
import com.anbui.data.models.CreateRoomRequest
import com.anbui.data.models.RoomResponse
import com.anbui.server
import com.anbui.utils.Constants
import com.anbui.utils.ResponseMessages
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Route for create room
 */
fun Route.createRoomRoute() {
    post("/api/createRoom") {
        // check parable
        val roomRequest = runCatching {
            call.receiveNullable<CreateRoomRequest>()
        }.getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        // check room's name exist
        if (server.rooms[roomRequest.name] != null) {
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    false,
                    ResponseMessages.ROOM_NAME_EXIST
                )
            )
            return@post
        }

        // check room min players
        if (roomRequest.maxPlayer < Constants.MIN_ROOM_SIZE) {
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    false,
                    ResponseMessages.VIOLATE_MIN_ROOM_SIZE,
                )
            )
            return@post
        }

        // check room max players
        if (roomRequest.maxPlayer > Constants.MAX_ROOM_SIZE) {
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    false,
                    ResponseMessages.VIOLATE_MAX_ROOM_SIZE

                )
            )
            return@post
        }
        val room = Room(
            roomRequest.name,
            roomRequest.maxPlayer
        )
        server.rooms[roomRequest.name] = room

        call.respond(HttpStatusCode.OK, BasicApiResponse(true))
    }
}

/**
 * Route for room searching
 */
fun Route.getRoomRoute() {
    get("api/getRooms") {
        val searchQuery = call.parameters["searchQuery"]
        if (searchQuery == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val roomResult = server.rooms.filterKeys {
            it.contains(searchQuery, ignoreCase = true)
        }

        val roomResponse = roomResult.values.map {
            RoomResponse(it.name, it.maxPlayer, it.players.size)
        }.sortedBy { it.name }

        call.respond(HttpStatusCode.OK, roomResponse)

    }
}
