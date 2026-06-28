package com.example.fitnesstracker.ui.screens.history

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.FitnessTrackerApplication
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.preferences.UnitSystem
import com.example.fitnesstracker.util.ActivityTypeFormatter
import com.example.fitnesstracker.util.DateFormatter
import com.example.fitnesstracker.util.DistanceFormatter
import com.example.fitnesstracker.util.DurationFormatter
import java.util.Locale

private data class ChipOption<T>(
    val value: T,
    @param:StringRes val labelRes: Int
)

private val activityTypeOptions: List<ChipOption<String?>> = listOf(
    ChipOption(value = null, labelRes = R.string.filter_all_types),
    ChipOption(value = "walking", labelRes = R.string.activity_type_walking),
    ChipOption(value = "running", labelRes = R.string.activity_type_running),
    ChipOption(value = "cycling", labelRes = R.string.activity_type_cycling)
)

private val datePresetOptions = listOf(
    ChipOption(value = HistoryDatePreset.ANY_TIME, labelRes = R.string.filter_any_time),
    ChipOption(value = HistoryDatePreset.LAST_7_DAYS, labelRes = R.string.filter_last_7_days),
    ChipOption(value = HistoryDatePreset.LAST_30_DAYS, labelRes = R.string.filter_last_30_days)
)

private val minimumDurationOptions: List<ChipOption<Long?>> = listOf(
    ChipOption(value = null, labelRes = R.string.filter_any_duration),
    ChipOption(value = 15L * 60L, labelRes = R.string.filter_duration_15_minutes),
    ChipOption(value = 30L * 60L, labelRes = R.string.filter_duration_30_minutes),
    ChipOption(value = 60L * 60L, labelRes = R.string.filter_duration_60_minutes)
)

@Composable
fun HistoryScreen(
    unitSystem: UnitSystem,
    onOpenActivityDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as FitnessTrackerApplication
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.provideFactory(application.activityRepository)
    )
    val uiState by historyViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FilterSection(
                uiState = uiState,
                onSearchQueryChanged = historyViewModel::updateSearchQuery,
                onTypeSelected = historyViewModel::updateType,
                onDatePresetSelected = historyViewModel::updateDatePreset,
                onMinimumDurationSelected = historyViewModel::updateMinimumDuration
            )
        }

        if (uiState.activities.isEmpty()) {
            item {
                EmptyHistoryCard(
                    hasSavedActivities = uiState.totalActivityCount > 0
                )
            }
        } else {
            items(
                items = uiState.activities,
                key = { activity -> activity.id }
            ) { activity ->
                ActivityHistoryCard(
                    activity = activity,
                    unitSystem = unitSystem,
                    onOpenDetails = { onOpenActivityDetails(activity.id) }
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    uiState: HistoryUiState,
    onSearchQueryChanged: (String) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onDatePresetSelected: (HistoryDatePreset) -> Unit,
    onMinimumDurationSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 700.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.filter.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.search_label)) },
                placeholder = { Text(text = stringResource(R.string.history_search_placeholder)) }
            )

            FilterChipRow(
                title = stringResource(R.string.filter_type_title),
                options = activityTypeOptions,
                selectedValue = uiState.filter.type,
                onSelected = onTypeSelected
            )

            FilterChipRow(
                title = stringResource(R.string.filter_date_title),
                options = datePresetOptions,
                selectedValue = uiState.filter.datePreset,
                onSelected = onDatePresetSelected
            )

            FilterChipRow(
                title = stringResource(R.string.filter_duration_title),
                options = minimumDurationOptions,
                selectedValue = uiState.filter.minimumDurationSeconds,
                onSelected = onMinimumDurationSelected
            )
        }
    }
}

@Composable
private fun <T> FilterChipRow(
    title: String,
    options: List<ChipOption<T>>,
    selectedValue: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = options, key = { option -> option.labelRes }) { option ->
                FilterChip(
                    selected = selectedValue == option.value,
                    onClick = { onSelected(option.value) },
                    label = { Text(text = stringResource(option.labelRes)) }
                )
            }
        }
    }
}

@Composable
private fun ActivityHistoryCard(
    activity: ActivityEntity,
    unitSystem: UnitSystem,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 700.dp)
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activityTypeDisplayName(activity.type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = DateFormatter.formatDateTime(activity.startTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistoryMetric(
                    label = stringResource(R.string.duration_label),
                    value = DurationFormatter.formatDuration(activity.durationSeconds)
                )
                HistoryMetric(
                    label = stringResource(R.string.distance_label),
                    value = stringResource(
                        R.string.value_with_unit,
                        DistanceFormatter.formatDistance(activity.distanceMeters, unitSystem),
                        stringResource(
                            if (unitSystem == UnitSystem.METRIC) R.string.unit_km else R.string.unit_mi
                        )
                    )
                )
            }

            if (!activity.description.isNullOrBlank()) {
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = stringResource(R.string.history_open_details_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HistoryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EmptyHistoryCard(
    hasSavedActivities: Boolean,
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
            Text(
                text = if (hasSavedActivities) {
                    stringResource(R.string.history_no_results_title)
                } else {
                    stringResource(R.string.history_empty_title)
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (hasSavedActivities) {
                    stringResource(R.string.history_no_results_body)
                } else {
                    stringResource(R.string.history_empty_body)
                },
                style = MaterialTheme.typography.bodyMedium
            )
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
