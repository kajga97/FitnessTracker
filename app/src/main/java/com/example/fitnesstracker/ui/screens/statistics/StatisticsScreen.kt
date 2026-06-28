package com.example.fitnesstracker.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.FitnessTrackerApplication
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.preferences.UnitSystem
import com.example.fitnesstracker.util.DistanceFormatter
import com.example.fitnesstracker.util.DurationFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatisticsScreen(
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as FitnessTrackerApplication
    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModel.provideFactory(
            activityRepository = application.activityRepository,
            goalRepository = application.goalRepository
        )
    )
    val uiState by statisticsViewModel.uiState.collectAsState()

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
                    .widthIn(max = 700.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.statistics_chart_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    SevenDayDistanceChart(
                        stats = uiState.sevenDayDistanceStats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }
        }

        item {
            GoalCard(
                title = stringResource(R.string.daily_goal_title),
                inputLabel = stringResource(
                    R.string.daily_goal_input_label,
                    stringResource(
                        if (unitSystem == UnitSystem.METRIC) R.string.unit_km else R.string.unit_mi
                    )
                ),
                inputValue = uiState.dailyDistanceGoalInput,
                progress = uiState.dailyDistanceGoalProgress,
                progressText = stringResource(
                    R.string.daily_goal_progress_value,
                    DistanceFormatter.formatDistance(uiState.dailyDistanceGoalProgress.currentValue, unitSystem),
                    stringResource(
                        if (unitSystem == UnitSystem.METRIC) R.string.unit_km else R.string.unit_mi
                    ),
                    DistanceFormatter.formatDistance(uiState.dailyDistanceGoalProgress.targetValue, unitSystem),
                    stringResource(
                        if (unitSystem == UnitSystem.METRIC) R.string.unit_km else R.string.unit_mi
                    ),
                    uiState.dailyDistanceGoalProgress.progressPercent
                ),
                onInputChange = statisticsViewModel::updateDailyDistanceGoalInput,
                onSave = { statisticsViewModel.saveDailyDistanceGoal(unitSystem) }
            )
        }

        item {
            GoalCard(
                title = stringResource(R.string.weekly_goal_title),
                inputLabel = stringResource(R.string.weekly_goal_input_label),
                inputValue = uiState.weeklyActiveTimeGoalInputHours,
                progress = uiState.weeklyActiveTimeGoalProgress,
                progressText = stringResource(
                    R.string.weekly_goal_progress_value,
                    DurationFormatter.formatDuration(uiState.weeklyActiveTimeGoalProgress.currentValue.toLong()),
                    DurationFormatter.formatDuration(uiState.weeklyActiveTimeGoalProgress.targetValue.toLong()),
                    uiState.weeklyActiveTimeGoalProgress.progressPercent
                ),
                onInputChange = statisticsViewModel::updateWeeklyActiveTimeGoalInputHours,
                onSave = statisticsViewModel::saveWeeklyActiveTimeGoal
            )
        }
    }
}

@Composable
private fun SevenDayDistanceChart(
    stats: List<DailyDistanceStat>,
    modifier: Modifier = Modifier
) {
    val maxDistance = stats.maxOfOrNull { it.totalDistanceMeters }?.takeIf { it > 0.0 } ?: 1.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (stats.isEmpty()) {
                return@Canvas
            }

            val spacing = 16.dp.toPx()
            val barWidth = (size.width - spacing * (stats.size - 1)) / stats.size

            stats.forEachIndexed { index, stat ->
                val barHeight = ((stat.totalDistanceMeters / maxDistance) * size.height).toFloat()
                val left = index * (barWidth + spacing)
                val top = size.height - barHeight

                drawRect(
                    color = surfaceVariant,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height)
                )

                drawRect(
                    color = primaryColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stats.forEach { stat ->
                Text(
                    text = stat.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    title: String,
    inputLabel: String,
    inputValue: String,
    progress: GoalProgressState,
    progressText: String,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
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
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = inputLabel) },
                singleLine = true
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.save_goal_button))
            }

            Text(text = progressText, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
