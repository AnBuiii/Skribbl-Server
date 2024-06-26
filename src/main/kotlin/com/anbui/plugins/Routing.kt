package com.anbui.plugins

import com.anbui.routes.createRoomRoute
import com.anbui.routes.gameWebSocketRoute
import com.anbui.routes.getRoomRoute
import com.anbui.routes.joinRoomRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Define all route for this Server
 */
fun Application.configureRouting() {
    routing {
        createRoomRoute()
        getRoomRoute()
        joinRoomRoute()

        gameWebSocketRoute()
    }
}
