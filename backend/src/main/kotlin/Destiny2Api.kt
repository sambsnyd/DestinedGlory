package com.github.sambsnyd.destinedglory

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.encodeURLPath
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

/**
 * A deeply incomplete wrapper for Bungie's Destiny 2 APIs, documented here:
 * https://bungie-net.github.io/multi/operation_get_Destiny2-SearchDestinyPlayer.html
 *
 */
class Destiny2Api(
        private val apiKey: String,
        /**
         * Requires an HttpClient that knows how to deserializse JSON!
         */
        private val httpClient: HttpClient = HttpClient(Apache) {
            install(JsonFeature)
        }
) {
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
        // No idea what "Demon" or "Next" mean in this context. Probably multiple different auth systems got merged
        Demon(10),
        Next(254),
        All(-1);

        companion object {
            fun from(value: Int): BungieMembershipType {
                return when(value) {
                    BungieMembershipType.None.value -> None
                    BungieMembershipType.Xbox.value -> Xbox
                    BungieMembershipType.Psn.value -> Psn
                    BungieMembershipType.Blizzard.value -> Blizzard
                    BungieMembershipType.Demon.value -> Demon
                    BungieMembershipType.Next.value -> Next
                    BungieMembershipType.All.value -> All
                    else -> throw IllegalArgumentException(
                            "Provided value $value doesn't correspond to any known BungieMembershipType")
                }
            }
        }
    }

    /**
     * PascalCasing on these value names is consistent with the bungie API responses and necessary for automatic
     * serial/deserialization without additional effort/mapping
     *
     * The rest of the names returned of their api are camelCase
     */
    data class BungieApiResponse<T>(
            val Response: T,
            val ErrorCode: Int,
            val ThrottleSeconds: Int,
            val ErrorStatus: String,
            val Message: String,
            val MessageData: Any
    )

    /**
     * This contract supplies basic information commonly used to display a minimal amount of information about a user.
     * https://bungie-net.github.io/multi/schema_User-UserInfoCard.html#schema_User-UserInfoCard
     */
    data class UserInfoCard(
            //TODO: Figure out how to get this field deserialized into a BungieMembershipType enum
            val membershipType: Int,

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
     * https://bungie-net.github.io/multi/schema_Destiny-HistoricalStats-DestinyHistoricalStatsValuePair.html#schema_Destiny-HistoricalStats-DestinyHistoricalStatsValuePair
     */
    data class DestinyHistoricalStatsValuePair(
            /**
             * Raw value of the statistic
             */
            val value: Double,
            /**
             * Localized & formatted version of the value
             */
            val displayValue: String)
    /**
     * https://bungie-net.github.io/multi/schema_Destiny-HistoricalStats-DestinyHistoricalStatsValue.html#schema_Destiny-HistoricalStats-DestinyHistoricalStatsValue
     */
    data class DestinyHistoricalStatsValue(
            val statId: String,
            val basic: DestinyHistoricalStatsValuePair,
            /**
             * Per-game-average, only relevant to some statistics
             */
            val pga: DestinyHistoricalStatsValuePair?)

    /**
     * The various aggregate statistics of a Destiny player's PvP career
     * There are more fields than these, omitted some I don't care about for brevity
     */
    data class PvpHistory(
            val activitiesEntered: DestinyHistoricalStatsValue,
            val activitiesWon: DestinyHistoricalStatsValue,
            val assists: DestinyHistoricalStatsValue,
            val totalDeathDistance: DestinyHistoricalStatsValue,
            val averageDeathDistance: DestinyHistoricalStatsValue,
            val totalKillDistance: DestinyHistoricalStatsValue,
            val kills: DestinyHistoricalStatsValue,
            val averageKillDistance: DestinyHistoricalStatsValue,
            val secondsPlayed: DestinyHistoricalStatsValue,
            val deaths: DestinyHistoricalStatsValue,
            val averageLifespan: DestinyHistoricalStatsValue,
            val averageScorePerKill: DestinyHistoricalStatsValue,
            val averageScorePerLife: DestinyHistoricalStatsValue,
            val bestSingleGameKills: DestinyHistoricalStatsValue,
            val bestSingleGameScore: DestinyHistoricalStatsValue,
            val opponentsDefeated: DestinyHistoricalStatsValue,
            val efficiency: DestinyHistoricalStatsValue,
            val killsDeathsRatio: DestinyHistoricalStatsValue,
            val killsDeathsAssists: DestinyHistoricalStatsValue,
            val objectivesCompleted: DestinyHistoricalStatsValue,
            val precisionKills: DestinyHistoricalStatsValue,
            val resurrectionsPerformed: DestinyHistoricalStatsValue,
            val resurrectionsReceived: DestinyHistoricalStatsValue,
            val suicides: DestinyHistoricalStatsValue,
            val weaponBestType: DestinyHistoricalStatsValue,
            val winLossRatio: DestinyHistoricalStatsValue,
            val allParticipantsCount: DestinyHistoricalStatsValue,
            val allParticipantsTimePlayed: DestinyHistoricalStatsValue,
            val longestKillSpree: DestinyHistoricalStatsValue,
            val longestSingleLife: DestinyHistoricalStatsValue,
            val totalActivityDurationSeconds: DestinyHistoricalStatsValue,
            /**
             * How good Bungie thinks you are at PvP overall
             */
            val combatRating: DestinyHistoricalStatsValue)
    data class AllPvp(val allTime: PvpHistory)
    data class HistoricalStats(
        val allPvP: AllPvp,
        //TODO: Look into anything except PvP at some unspecified point in the future when it matters to this project
        val patrol: Any,
        val raid: Any,
        val story: Any,
        val allStrikes: Any)

    /**
     * https://bungie-net.github.io/multi/operation_get_Destiny2-SearchDestinyPlayer.html#operation_get_Destiny2-SearchDestinyPlayer
     */
    suspend fun searchDestinyPlayer(membershipType: BungieMembershipType, playerName: String): BungieApiResponse<List<UserInfoCard>> =
        httpClient.get("$apiRoot/Destiny2/SearchDestinyPlayer/${membershipType.value}/${playerName.encodeURLPath()}/") {
            header(apiKeyHeaderName, apiKey)
        }

    /**
     * The default characterId of 0 means "aggregate results across all characters"
     * https://bungie-net.github.io/multi/operation_get_Destiny2-GetHistoricalStats.html#operation_get_Destiny2-GetHistoricalStats
     */
    suspend fun getHistoricalStats(membershipType: BungieMembershipType, membershipId: Long, characterId: Long=0): BungieApiResponse<HistoricalStats> =
            httpClient.get("$apiRoot/Destiny2/${membershipType.value}/Account/$membershipId/Character/$characterId/Stats") {
                header(apiKeyHeaderName, apiKey)
            }
}
