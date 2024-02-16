package com.anbui.plugins

import com.anbui.session.DrawingSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*

fun Application.configSession() {
    install(Sessions) {
        cookie<DrawingSession>("SESSIONS")
    }

    // Interceptor
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<DrawingSession>() == null) {
            val clientId = call.parameters["client"] ?: ""
            call.sessions.set(DrawingSession(clientId, generateNonce()))
        }
    }
}