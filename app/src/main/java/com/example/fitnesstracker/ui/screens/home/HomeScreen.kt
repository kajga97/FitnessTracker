package com.example.fitnesstracker.ui.screens.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R

private data class ActivityTypeOption(
    val routeValue: String,
    @param:StringRes val labelRes: Int
)

private val activityTypes = listOf(
    ActivityTypeOption(routeValue = "walking", labelRes = R.string.activity_type_walking),
    ActivityTypeOption(routeValue = "running", labelRes = R.string.activity_type_running),
    ActivityTypeOption(routeValue = "cycling", labelRes = R.string.activity_type_cycling)
)

@Composable
fun HomeScreen(
    onStartTracking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(activityTypes.first().routeValue) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_headline),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.select_activity_type),
                        style = MaterialTheme.typography.titleMedium
                    )

                    activityTypes.forEach { activityType ->
                        FilterChip(
                            selected = selectedType == activityType.routeValue,
                            onClick = { selectedType = activityType.routeValue },
                            label = { Text(text = stringResource(activityType.labelRes)) }
                        )
                    }

                    Button(
                        onClick = { onStartTracking(selectedType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.start_activity))
                    }
                }
            }
        }

        item {
            PlaceholderCard(
                title = stringResource(R.string.today_summary_title),
                body = stringResource(R.string.home_today_summary_placeholder)
            )
        }

        item {
            PlaceholderCard(
                title = stringResource(R.string.goal_progress_title),
                body = stringResource(R.string.home_goal_placeholder)
            )
        }

        item {
            PlaceholderCard(
                title = stringResource(R.string.recent_activity_title),
                body = stringResource(R.string.home_recent_placeholder)
            )
        }
    }
}

@Composable
private fun PlaceholderCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 600.dp)
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
