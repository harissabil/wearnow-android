package id.harissabil.wearnow.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object Splash : Route()

    @Serializable
    data object Auth : Route()

    @Serializable
    data object Onboarding : Route()

    @Serializable
    data object Home : Route()

    @Serializable
    data class Result(
        val historyId: String,
    ) : Route()

    @Serializable
    data object History : Route()
}