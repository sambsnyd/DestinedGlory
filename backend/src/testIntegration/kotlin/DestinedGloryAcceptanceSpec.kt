package com.github.sambsnyd.destinedglory

import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

/**
 * An end-to-end style test for verifying the server can start and serve a static asset or two
 */
object DestinedGloryAcceptanceSpec: Spek({
    // It's brittle to just hardcode a different port than we hardcode in the product code
    // TODO: Remember to dynamically select available port here, too, after implementing that functionality
    val hopefullyFreePort = 8009
    val host = "localhost"
    lateinit var server: ApplicationEngine
    beforeGroup {
        server = embeddedServer(
                factory = Netty,
                port = hopefullyFreePort,
                host = host,
                module = Application::destinedGloryModule)
        server.start()
    }

    given("a DestinedGloryServer") {
        val httpClient = HttpClient(Apache) {
            install(JsonFeature)
        }

        on("requesting static assets") {
            val indexHtml = runBlocking {
                httpClient.get<String>("http://$host:$hopefullyFreePort/index.html")
            }
            it("provides responds with non-empty index.html") {
                assertTrue(indexHtml.isNotBlank())
            }
        }
        afterGroup {
            server.stop(1000L, 1000L, TimeUnit.MILLISECONDS)
        }
    }
})
