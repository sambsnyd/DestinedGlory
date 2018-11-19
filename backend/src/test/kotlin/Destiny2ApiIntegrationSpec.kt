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
import kotlin.test.assertNotNull

/**
 * Specify how the API wrapper and the real API integrate
 * Makes real API calls and so requires a real api key
 */
object Destiny2ApiIntegrationSpec : Spek({
    given("Destiny2 API Wrapper") {
        val apiKey by memoized { System.getenv(Destiny2Api.apiKeyName) ?: throw IllegalArgumentException(
                "Must supply environment variable ${Destiny2Api.apiKeyName} to run integration tests")
        }
        val destiny2Api by memoized { Destiny2Api(apiKey) }

        on("requesting a specific user") {
            val searchPlayer = runBlocking {
                destiny2Api.searchDestinyPlayer(Destiny2Api.BungieMembershipType.Blizzard, "castor#11308")
            }
            it("response has exactly one user") {
                assertEquals(1, searchPlayer.Response.size)
            }
            it("has the expected user name") {
                assertEquals("Castor#11308", searchPlayer.Response.first().displayName)
            }
        }

        on("requesting a user's overall stats") {
            val playerStats = runBlocking {
                destiny2Api.getHistoricalStats(Destiny2Api.BungieMembershipType.Blizzard, 4611686018467499442L)
            }
            it("response has some stats") {
                // Mostly care that the response was able to be deserialized down to the leaf
                assertNotNull(playerStats.Response.allPvP.allTime.activitiesEntered.basic.value)
            }
        }
    }
})
