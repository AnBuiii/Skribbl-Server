package com.anbui.session

import com.anbui.plugins.configSession
import io.ktor.server.application.*

/**
 * Represent unique identifier for each [call][ApplicationCall], sent from HTTP request or Websocket.
 * Attach to [call] by *interceptor* define in [Application.configSession]
 * @param clientId get from [ApplicationCall.parameters] *client parameter*
 * @param sessionId generate by SERVER
 */
data class DrawingSession(
    val clientId: String,
    val sessionId: String
)
