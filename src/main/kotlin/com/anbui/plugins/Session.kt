package com.anbui.plugins

import com.anbui.session.DrawingSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*

/**
 * Config basic [Sessions] for SERVER
 * @see DrawingSession
 */
fun Application.configSession() {
    install(Sessions) {
        cookie<DrawingSession>("SESSIONS")
    }

    install(sessionInterceptorPlugin)
}

val sessionInterceptorPlugin = createApplicationPlugin("sessionInterceptorPlugin") {
    onCall { call ->
        if (call.sessions.get<DrawingSession>() == null) {
            val clientId = call.parameters["client"] ?: ""
            call.sessions.set(DrawingSession(clientId, generateNonce()))
        }
    }
}
