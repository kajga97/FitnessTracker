package com.example.fitnesstracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnesstracker.data.preferences.AppSettings
import com.example.fitnesstracker.ui.screens.details.ActivityDetailsScreen
import com.example.fitnesstracker.ui.screens.history.HistoryScreen
import com.example.fitnesstracker.ui.screens.home.HomeScreen
import com.example.fitnesstracker.ui.screens.settings.SettingsScreen
import com.example.fitnesstracker.ui.screens.statistics.StatisticsScreen
import com.example.fitnesstracker.ui.screens.tracking.TrackingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessTrackerApp(
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val isTopLevelRoute = Screen.isTopLevelRoute(currentRoute)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(Screen.titleResForRoute(currentRoute))) },
                navigationIcon = {
                    if (!isTopLevelRoute && navController.previousBackStackEntry != null) {
                        IconButton(onClick = navController::navigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = com.example.fitnesstracker.R.string.back_button)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isTopLevelRoute) {
                NavigationBar {
                    Screen.topLevelDestinations.forEach { destination ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any {
                            it.route == destination.destinationRoute
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.destinationRoute) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.labelRes)
                                )
                            },
                            label = { Text(text = stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartTracking = { activityType ->
                        navController.navigate(Screen.Tracking.createRoute(activityType))
                    }
                )
            }
            composable(
                route = Screen.Tracking.route,
                arguments = listOf(navArgument("activityType") { type = NavType.StringType })
            ) { backStackEntry ->
                val activityType = backStackEntry.arguments?.getString("activityType").orEmpty()
                TrackingScreen(
                    activityType = activityType,
                    unitSystem = settings.unitSystem,
                    onTrackingSaved = navController::navigateUp
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    unitSystem = settings.unitSystem,
                    onOpenActivityDetails = { activityId ->
                        navController.navigate(Screen.Details.createRoute(activityId))
                    }
                )
            }
            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("activityId") { type = NavType.LongType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getLong("activityId") ?: 0L
                ActivityDetailsScreen(
                    activityId = activityId,
                    unitSystem = settings.unitSystem,
                    onDeleteComplete = navController::navigateUp
                )
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(unitSystem = settings.unitSystem)
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
