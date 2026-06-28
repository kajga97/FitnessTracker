package com.example.fitnesstracker.ui.screens.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.BuildConfig
import com.example.fitnesstracker.FitnessTrackerApplication
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.RoutePointEntity
import com.example.fitnesstracker.data.preferences.UnitSystem
import com.example.fitnesstracker.util.ActivityTypeFormatter
import com.example.fitnesstracker.util.DateFormatter
import com.example.fitnesstracker.util.DistanceFormatter
import com.example.fitnesstracker.util.DurationFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

@Composable
fun ActivityDetailsScreen(
    activityId: Long,
    unitSystem: UnitSystem,
    onDeleteComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as FitnessTrackerApplication
    val detailsViewModel: ActivityDetailsViewModel = viewModel(
        factory = ActivityDetailsViewModel.provideFactory(
            activityId = activityId,
            activityRepository = application.activityRepository
        )
    )
    val uiState by detailsViewModel.uiState.collectAsState()
    val activity = uiState.activity

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleteComplete()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            when {
                uiState.isLoading -> {
                    MessageCard(
                        title = stringResource(R.string.details_loading_title),
                        body = stringResource(R.string.details_loading_body)
                    )
                }

                activity == null -> {
                    MessageCard(
                        title = stringResource(R.string.details_not_found_title),
                        body = stringResource(R.string.details_not_found_body)
                    )
                }

                else -> {
                    ActivityDetailsCard(
                        activity = activity,
                        routePoints = uiState.routePoints,
                        unitSystem = unitSystem,
                        onDeleteActivity = detailsViewModel::deleteActivity
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityDetailsCard(
    activity: ActivityEntity,
    routePoints: List<RoutePointEntity>,
    unitSystem: UnitSystem,
    onDeleteActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 700.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailField(
                label = stringResource(R.string.type_label),
                value = activityTypeDisplayName(activity.type)
            )
            DetailField(
                label = stringResource(R.string.date_time_label),
                value = DateFormatter.formatDateTime(activity.startTime)
            )
            DetailField(
                label = stringResource(R.string.duration_label),
                value = DurationFormatter.formatDuration(activity.durationSeconds)
            )
            DetailField(
                label = stringResource(R.string.total_distance_label),
                value = stringResource(
                    R.string.value_with_unit,
                    DistanceFormatter.formatDistance(activity.distanceMeters, unitSystem),
                    stringResource(
                        if (unitSystem == UnitSystem.METRIC) R.string.unit_km else R.string.unit_mi
                    )
                )
            )
            DetailField(
                label = stringResource(R.string.average_speed_label),
                value = stringResource(
                    R.string.value_with_unit,
                    DistanceFormatter.formatSpeed(activity.averageSpeedMetersPerSecond, unitSystem),
                    stringResource(
                        if (unitSystem == UnitSystem.METRIC) R.string.unit_kmh else R.string.unit_mph
                    )
                )
            )
            DetailField(
                label = stringResource(R.string.description_label),
                value = activity.description?.takeIf { description ->
                    description.isNotBlank()
                } ?: stringResource(R.string.no_description_value)
            )

            RouteMapSection(routePoints = routePoints)

            Button(
                onClick = onDeleteActivity,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.delete_activity_button))
            }
        }
    }
}

@Composable
private fun RouteMapSection(
    routePoints: List<RoutePointEntity>,
    modifier: Modifier = Modifier
) {
    val mapApiKeyConfigured = BuildConfig.MAPS_API_KEY.isNotBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.route_map_title),
            style = MaterialTheme.typography.titleMedium
        )

        when {
            routePoints.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_route_available),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            !mapApiKeyConfigured -> {
                Text(
                    text = stringResource(R.string.map_api_key_missing_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                RouteMap(routePoints = routePoints)
            }
        }
    }
}

@Composable
private fun RouteMap(
    routePoints: List<RoutePointEntity>,
    modifier: Modifier = Modifier
) {
    val latLngPoints = routePoints.map { routePoint ->
        LatLng(routePoint.latitude, routePoint.longitude)
    }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(latLngPoints) {
        if (latLngPoints.isEmpty()) {
            return@LaunchedEffect
        }

        if (latLngPoints.size == 1) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(latLngPoints.first(), 15f)
            )
        } else {
            val boundsBuilder = LatLngBounds.builder()
            latLngPoints.forEach(boundsBuilder::include)
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
            )
        }
    }

    val startPoint = latLngPoints.firstOrNull()
    val endPoint = latLngPoints.lastOrNull()
    val startMarkerState = startPoint?.let { point ->
        remember(point) {
            com.google.maps.android.compose.MarkerState(position = point)
        }
    }
    val endMarkerState = endPoint?.let { point ->
        remember(point) {
            com.google.maps.android.compose.MarkerState(position = point)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxWidth(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false
            )
        ) {
            Polyline(points = latLngPoints)

            if (startPoint != null && startMarkerState != null) {
                Marker(
                    state = startMarkerState,
                    title = stringResource(R.string.route_start_marker_title)
                )
            }

            if (endPoint != null && endPoint != startPoint && endMarkerState != null) {
                Marker(
                    state = endMarkerState,
                    title = stringResource(R.string.route_end_marker_title)
                )
            }
        }
    }
}

@Composable
private fun activityTypeDisplayName(type: String): String {
    val labelRes = ActivityTypeFormatter.labelResOrNull(type)

    return labelRes?.let { stringResource(it) } ?: type.replaceFirstChar { firstChar ->
        if (firstChar.isLowerCase()) {
            firstChar.titlecase(Locale.getDefault())
        } else {
            firstChar.toString()
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun MessageCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 700.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
