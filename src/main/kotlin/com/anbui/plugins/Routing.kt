package com.anbui.plugins

import com.anbui.routes.createRoomRoute
import com.anbui.routes.getRoomRoute
import com.anbui.routes.joinRoomRoute
import com.anbui.routes.standardWebSocket
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        createRoomRoute()
        getRoomRoute()
        joinRoomRoute()
        standardWebSocket({ _, _, _, _ -> })
    }
}
