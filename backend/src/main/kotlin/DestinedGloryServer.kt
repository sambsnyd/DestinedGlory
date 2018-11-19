package com.github.sambsnyd.destinedglory

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("main")

fun main(){
    val server = embeddedServer(
            factory = Netty,
            port = 8008, // TODO: Dynamically select an available port
            host = "localhost",
            watchPaths = listOf("build/libs"), // Best-effort reload running server if a new jar is built
            module = Application::destinedGloryModule)
    server.start(wait = true)
}
// Writing in this style to facilitate hot-reloading on content changes
// Per https://ktor.io/servers/autoreload.html
fun Application.destinedGloryModule() {
    // There are various perspectives on the best way to securely pass around secrets
    // But just to get things started for this low-severity secret, we'll use the environment
    val bungieApiKey: String = System.getenv(Destiny2Api.apiKeyName) ?:
    throw IllegalArgumentException("No API key specified via the \"${Destiny2Api.apiKeyName}\" environment variable. " +
            "No new Guardian information will be able to be queried! " +
            "Get an API key at https://www.bungie.net/en/Application")
    val guardian = "guardian"
    val destiny2Api = Destiny2Api(bungieApiKey)

    // Enable JSON serialization/deserialization via gson
    install(ContentNegotiation) {
        gson {
            // Prioritize developer convenience over saving every possible byte going over the wire
            setPrettyPrinting()
        }
    }
    install(AutoHeadResponse)
    routing {
        static("/") {
            resources("staticassets")
            defaultResource("staticassets/index.html")
        }
        get("/guardian/{$guardian}") getGuardian@{
            log.debug("GET /guardian/$guardian")
            val guardianName = call.parameters[guardian]
            if(guardianName.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "guardian parameter must have a value")
                return@getGuardian
            }
            val searchPlayerResponse = destiny2Api.searchDestinyPlayer(Destiny2Api.BungieMembershipType.All, guardianName)
            if(searchPlayerResponse.Response.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "Couldn't find a Guardian named $guardianName")
                return@getGuardian
            }
            val player = searchPlayerResponse.Response.first()

            log.debug("GET /guardian/$guardian found player ${player.displayName} with id ${player.membershipId}")
            val crucibleStats = destiny2Api.getHistoricalStats(Destiny2Api.BungieMembershipType.from(player.membershipType), player.membershipId)
            call.respond(HttpStatusCode.OK, crucibleStats)
        }
    }
}
