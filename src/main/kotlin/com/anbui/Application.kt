package com.anbui

import com.anbui.plugins.*
import com.anbui.session.DrawingSession
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.sessions.*
import io.ktor.util.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    install(Sessions) {
        cookie<DrawingSession>("SESSIONS")
    }

    intercept(Plugins) {
        if (call.sessions.get<DrawingSession>() == null) {
            val clientId = call.parameters["client"] ?: ""
            call.sessions.set(DrawingSession(clientId, generateNonce()))
        }
    }
    configureSerialization()
    configureSockets()
    configureMonitoring()
    configureRouting()
}
