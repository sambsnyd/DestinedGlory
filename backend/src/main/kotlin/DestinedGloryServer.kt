package com.github.sambsnyd.destinedglory

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.features.AutoHeadResponse
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

fun main(){
    val log = LoggerFactory.getLogger("DestinedGloryServer")
    val constants = object {
        val guardian = "guardian"
        val apiKeyName = "bungieapikey"
    }
    val bungieApiKey: String? = System.getenv(constants.apiKeyName)
    if(bungieApiKey == null) {
        log.warn("No API key specified via the \"${constants.apiKeyName}\" environment variable. " +
                "No new Guardian information will be able to be queried! " +
                "Get an API key at https://www.bungie.net/en/Application")
    }

    // We will be issuing HTTP requests to other APIs
    //val httpClient = HttpClient(Apache)

    val server = embeddedServer(Netty, 8008) {

        // Enable JSON serialization/deserialization
        install(ContentNegotiation) {
            jackson {
                // Currently prioritizing development experience over other considerations
                // So let's send pretty-printed JSON when applicable
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }
        install(AutoHeadResponse)
        routing {
            static("/") {
                resources("staticassets")
                defaultResource("staticassets/index.html")
            }
            get("/guardian/{${constants.guardian}}") {
                val guardianName = call.parameters[constants.guardian]
                if(guardianName.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "guardian parameter must have a value")
                } else {
                    call.respondText("Requested stats for $guardianName", ContentType.Text.Plain)
                }
            }
        }
    }
    server.start(wait = true)
}
