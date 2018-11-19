package com.github.sambsnyd.destinedglory

import io.ktor.client.HttpClient

import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import kotlinx.coroutines.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals

/**
 * Specify how the API wrapper and the real API integrate
 * Makes real API calls and so requires a real api key
 */
object Destiny2ApiIntegrationSpec : Spek({
    given("Destiny2 API Wrapper") {
        val apiKey by memoized { System.getenv(Destiny2Api.apiKeyName) ?: throw IllegalArgumentException(
                "Must supply environment variable ${Destiny2Api.apiKeyName} to run integration tests")
        }
        val destiny2Api by memoized { Destiny2Api(HttpClient(Apache) {
            install(JsonFeature)
        }, apiKey) }

        on("Fetching a specific user") {
            runBlocking {
                val player = destiny2Api.searchDestinyPlayer(Destiny2Api.BungieMembershipType.Blizzard, "castor#11308")
                it("responds with error code 0") {
                    assertEquals(0, player.errorCode)
                }
            }
        }
    }
})
