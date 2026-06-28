package com.example.fitnesstracker.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.FitnessTrackerApplication
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.preferences.ThemeMode
import com.example.fitnesstracker.data.preferences.UnitSystem

private data class SettingsOption<T>(
    val value: T,
    @param:StringRes val labelRes: Int
)

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as FitnessTrackerApplication
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(application.settingsRepository)
    )
    val settings by settingsViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsOptionsCard(
                title = stringResource(R.string.language_setting_title),
                options = listOf(
                    SettingsOption("en", R.string.language_english),
                    SettingsOption("sr", R.string.language_serbian)
                ),
                selectedValue = settings.languageCode,
                onSelected = settingsViewModel::updateLanguage
            )
        }

        item {
            SettingsOptionsCard(
                title = stringResource(R.string.units_setting_title),
                options = listOf(
                    SettingsOption(UnitSystem.METRIC, R.string.units_metric),
                    SettingsOption(UnitSystem.IMPERIAL, R.string.units_imperial)
                ),
                selectedValue = settings.unitSystem,
                onSelected = settingsViewModel::updateUnitSystem
            )
        }

        item {
            SettingsToggleCard(
                title = stringResource(R.string.notifications_setting_title),
                body = stringResource(R.string.notifications_setting_description),
                checked = settings.notificationsEnabled,
                onCheckedChange = { enabled ->
                    settingsViewModel.updateNotificationsEnabled(context, enabled)
                }
            )
        }

        item {
            SettingsOptionsCard(
                title = stringResource(R.string.theme_setting_title),
                options = listOf(
                    SettingsOption(ThemeMode.SYSTEM, R.string.theme_system),
                    SettingsOption(ThemeMode.LIGHT, R.string.theme_light),
                    SettingsOption(ThemeMode.DARK, R.string.theme_dark)
                ),
                selectedValue = settings.themeMode,
                onSelected = settingsViewModel::updateThemeMode
            )
        }
    }
}

@Composable
private fun <T> SettingsOptionsCard(
    title: String,
    options: List<SettingsOption<T>>,
    selectedValue: T,
    onSelected: (T) -> Unit,
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(options, key = { option -> option.labelRes }) { option ->
                    FilterChip(
                        selected = selectedValue == option.value,
                        onClick = { onSelected(option.value) },
                        label = { Text(text = stringResource(option.labelRes)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
