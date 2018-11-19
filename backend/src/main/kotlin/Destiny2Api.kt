package com.github.sambsnyd.destinedglory

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import org.slf4j.LoggerFactory

/**
 * A deeply incomplete wrapper for Bungie's Destiny 2 APIs, documented here:
 * https://bungie-net.github.io/multi/operation_get_Destiny2-SearchDestinyPlayer.html
 *
 */
class Destiny2Api(
        private val httpClient: HttpClient,
        private val apiKey: String) {

    companion object {
        const val apiRoot = "https://www.bungie.net/Platform/"
        const val apiKeyName = "bungieapikey"
        const val apiKeyHeaderName = "X-API-Key"
        private val log = LoggerFactory.getLogger(Destiny2Api::class.java)
    }

    enum class BungieMembershipType(val value: Int) {
        None(0),
        Xbox(1),
        Psn(2),
        Blizzard(4),
        Demon(10),
        Next(254),
        All(-1)
    }

    data class BungieApiResponse<T>(
            //val response: T,
            val errorCode: Int,
            val throttleSeconds: Int,
            val errorStatus: String,
            val Message: String,
            val MessageData: Any
    )

    /**
     * This contract supplies basic information commonly used to display a minimal amount of information about a user.
     * https://bungie-net.github.io/multi/schema_User-UserInfoCard.html#schema_User-UserInfoCard
     */
    data class UserInfoCard(
            val membershipType: BungieMembershipType,
            /**
             * Membership ID as they user is known in the Accounts service
             */
            val membershipId: Long,
            /**
             * Display Name the player has chosen for themselves.
             * The display name is optional when the data type is used as input to a platform API.
             */
            val displayName: String)

    /**
     * https://bungie-net.github.io/multi/operation_get_Destiny2-SearchDestinyPlayer.html#operation_get_Destiny2-SearchDestinyPlayer
     */
    suspend fun searchDestinyPlayer(membershipType: BungieMembershipType, playerName: String): BungieApiResponse<Any?> {
        // Blizzard names have a '#' in them which needs escaping
        val escapedName = playerName.replace("#", "%23")
        return httpClient.get("$apiRoot/Destiny2/SearchDestinyPlayer/${membershipType.value}/$escapedName/") {
            header(apiKeyHeaderName, apiKey)
        }
    }
}
