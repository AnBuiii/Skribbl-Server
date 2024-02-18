package com.anbui.plugins

import io.ktor.server.application.*
import io.ktor.server.websocket.*
import java.time.Duration

/**
 * Basic config for SERVER websocket
 * @see WebSockets
 */
fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
