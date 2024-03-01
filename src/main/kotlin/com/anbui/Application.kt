package com.anbui

import com.anbui.data.local.SkribblDatabase
import com.anbui.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val server = DrawingServer()

@Suppress("unused")
fun Application.module() {
    SkribblDatabase.init()
    configSession()
    configureSerialization()
    configureSockets()
    configureMonitoring()
    configureRouting()
}
