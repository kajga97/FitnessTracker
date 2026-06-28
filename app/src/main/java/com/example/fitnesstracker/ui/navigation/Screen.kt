package com.example.fitnesstracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fitnesstracker.R

sealed class Screen(
    val route: String,
    @param:StringRes val titleRes: Int
) {
    data class TopLevel(
        val destinationRoute: String,
        @param:StringRes val labelRes: Int,
        val icon: ImageVector
    )

    data object Home : Screen("home", R.string.home_title)
    data object Tracking : Screen("tracking/{activityType}", R.string.tracking_title) {
        fun createRoute(activityType: String): String = "tracking/$activityType"
    }

    data object History : Screen("history", R.string.history_title)
    data object Details : Screen("details/{activityId}", R.string.details_title) {
        fun createRoute(activityId: Long): String = "details/$activityId"
    }

    data object Statistics : Screen("statistics", R.string.statistics_title)
    data object Settings : Screen("settings", R.string.settings_title)

    companion object {
        val topLevelDestinations = listOf(
            TopLevel(
                destinationRoute = Home.route,
                labelRes = R.string.home_title,
                icon = Icons.Filled.Home
            ),
            TopLevel(
                destinationRoute = History.route,
                labelRes = R.string.history_title,
                icon = Icons.AutoMirrored.Filled.List
            ),
            TopLevel(
                destinationRoute = Statistics.route,
                labelRes = R.string.statistics_title,
                icon = Icons.AutoMirrored.Filled.ShowChart
            ),
            TopLevel(
                destinationRoute = Settings.route,
                labelRes = R.string.settings_title,
                icon = Icons.Filled.Settings
            )
        )

        fun titleResForRoute(route: String?): Int = when {
            route == null -> R.string.app_name
            route.startsWith("tracking") -> Tracking.titleRes
            route.startsWith("details") -> Details.titleRes
            route == Home.route -> Home.titleRes
            route == History.route -> History.titleRes
            route == Statistics.route -> Statistics.titleRes
            route == Settings.route -> Settings.titleRes
            else -> R.string.app_name
        }

        fun isTopLevelRoute(route: String?): Boolean {
            return topLevelDestinations.any { it.destinationRoute == route }
        }
    }
}
