package com.anbui

import com.anbui.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val server = DrawingServer()

@Suppress("unused")
fun Application.module() {
    configSession()
    configureSerialization()
    configureSockets()
    configureMonitoring()
    configureRouting()
}
