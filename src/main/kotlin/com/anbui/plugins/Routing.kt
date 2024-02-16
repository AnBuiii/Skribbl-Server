package com.anbui.plugins

import com.anbui.routes.createRoomRoute
import com.anbui.routes.getRoomRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        createRoomRoute()
        getRoomRoute()
    }
}
