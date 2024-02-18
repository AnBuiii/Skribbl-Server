package com.anbui.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

/**
 * Basic config for Server serialization using Kotlin's Serialization
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
