package com.github.sambsnyd.destinedglory

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.http.ContentType
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(){
    val server = embeddedServer(Netty, 8008) {

        install(AutoHeadResponse)
        routing {
            static("/") {
                resources("staticassets")
                defaultResource("staticassets/index.html")
            }
            get("/hello") {
                call.respondText("Hello world", ContentType.Text.Plain)
            }
        }
    }
    server.start(wait = true)
}
