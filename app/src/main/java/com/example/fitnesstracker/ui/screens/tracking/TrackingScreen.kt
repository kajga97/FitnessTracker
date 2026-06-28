package com.example.fitnesstracker.ui.screens.tracking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.FitnessTrackerApplication
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.preferences.UnitSystem
import com.example.fitnesstracker.tracking.TrackingService
import com.example.fitnesstracker.util.ActivityTypeFormatter
import com.example.fitnesstracker.util.DistanceFormatter
import com.example.fitnesstracker.util.DurationFormatter
import java.util.Locale

@Composable
fun TrackingScreen(
    activityType: String,
    unitSystem: UnitSystem,
    onTrackingSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as FitnessTrackerApplication
    val context = LocalContext.current
    val trackingViewModel: TrackingViewModel = viewModel(
        factory = TrackingViewModel.provideFactory(
            activityRepository = application.activityRepository,
            trackingManager = application.trackingManager
        )
    )
    val trackingState by trackingViewModel.trackingState.collectAsState()
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var description by rememberSaveable { mutableStateOf("") }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val locationPermissionsGranted = remember(context) {
        {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResults ->
        val locationGranted = permissionResults[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissionResults[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            locationPermissionsGranted()

        if (locationGranted) {
            trackingViewModel.clearPermissionDenied()
            trackingViewModel.startTracking(activityType)
            TrackingService.start(context)
        } else {
            trackingViewModel.markPermissionDenied()
        }
    }

    LaunchedEffect(activityType) {
        if (locationPermissionsGranted()) {
            trackingViewModel.startTracking(activityType)
            TrackingService.start(context)
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(text = stringResource(R.string.save_activity_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(R.string.save_activity_dialog_body))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.description_label)) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        TrackingService.stop(context)
                        trackingViewModel.stopAndSave(description) {
                            description = ""
                            onTrackingSaved()
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.save_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(text = stringResource(R.string.cancel_button))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.tracking_selected_type,
                            activityTypeDisplayName(activityType)
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )

                    TrackingMetric(
                        label = stringResource(R.string.duration_label),
                        value = DurationFormatter.formatDuration(trackingState.elapsedSeconds)
                    )
                    TrackingMetric(
                        label = stringResource(R.string.distance_label),
                        value = stringResource(
                            R.string.value_with_unit,
                            DistanceFormatter.formatDistance(trackingState.distanceMeters, unitSystem),
                            stringResource(
                                if (unitSystem == UnitSystem.METRIC) R.string.unit_km else R.string.unit_mi
                            )
                        )
                    )
                    TrackingMetric(
                        label = stringResource(R.string.current_speed_label),
                        value = stringResource(
                            R.string.value_with_unit,
                            DistanceFormatter.formatSpeed(
                                trackingState.currentSpeedMetersPerSecond,
                                unitSystem
                            ),
                            stringResource(
                                if (unitSystem == UnitSystem.METRIC) R.string.unit_kmh else R.string.unit_mph
                            )
                        )
                    )

                    Text(
                        text = when {
                            trackingState.locationPermissionDenied -> {
                                stringResource(R.string.tracking_permission_required_message)
                            }

                            trackingState.isPaused -> {
                                stringResource(R.string.tracking_paused_message)
                            }

                            else -> {
                                stringResource(R.string.tracking_live_message)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (trackingState.isPaused) {
                                    trackingViewModel.resumeTracking()
                                } else {
                                    trackingViewModel.pauseTracking()
                                }
                            },
                            enabled = trackingState.isTracking || trackingState.isPaused,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(
                                    if (trackingState.isPaused) {
                                        R.string.resume_button
                                    } else {
                                        R.string.pause_button
                                    }
                                )
                            )
                        }
                        Button(
                            onClick = { showSaveDialog = true },
                            enabled = trackingState.elapsedSeconds > 0L || trackingState.routePoints.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.stop_button))
                        }
                    }

                    if (trackingState.locationPermissionDenied) {
                        Button(
                            onClick = { permissionLauncher.launch(requiredPermissions) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.grant_permission_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Start
        )
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
