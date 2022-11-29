package com.brian.weathercompose.presentation.screens.settings


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weathercompose.R
import com.brian.weathercompose.data.settings.PreferencesRepository
import com.brian.weathercompose.presentation.SettingsDrawerItem
import com.brian.weathercompose.presentation.SettingsListItem
import com.brian.weathercompose.presentation.screens.reusablecomposables.LabeledCheckBox
import com.brian.weathercompose.presentation.screens.reusablecomposables.SettingsListItemWithCheckbox
import com.brian.weathercompose.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun NotificationSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    preferencesRepository: PreferencesRepository,
    locations: List<String>
) {
    val showDialog = remember { mutableStateOf(false) }
    viewModel.updateActionBarTitle(LocalContext.current.getString(R.string.notifications_settings))
    val itemsList = prepareNotificationSettings()

    val showNotifications = preferencesRepository.getNotificationSetting.collectAsState(initial = null)
    val showLocalForecast = preferencesRepository.getLocalForecastSetting.collectAsState(initial = null)
    val showPrecipitationNotifications = preferencesRepository.getPrecipitationSetting.collectAsState(
        initial = null
    )
    val selectedLocations = preferencesRepository.getPrecipitationLocations.collectAsState(initial = null)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {

        item {
            SettingsListItemWithCheckbox(
                item = itemsList[0],
                isChecked = showNotifications.value ?: true,
                onCheckedChanged = {
                    coroutineScope.launch {
                        preferencesRepository.saveNotificationSetting(!showNotifications.value!!)
                    }
                }
            )
        }

        item {
            SettingsListItemWithCheckbox(
                item = itemsList[1],
                isChecked = showLocalForecast.value ?: true && showNotifications.value ?: true,
                onCheckedChanged = {
                    coroutineScope.launch {
                        preferencesRepository.saveLocalForecastSetting(!showLocalForecast.value!!)
                    }
                },
                enabled = showNotifications.value ?: true
            )
        }

        item {
            SettingsListItemWithCheckbox(
                item = itemsList[2],
                isChecked = showPrecipitationNotifications.value ?: true && showNotifications.value ?: true,
                onCheckedChanged = {
                    coroutineScope.launch {
                        preferencesRepository.savePrecipitationSetting(!showPrecipitationNotifications.value!!)
                    }
                },
                enabled = showNotifications.value ?: true
            )
        }

        item {
            SettingsListItem(
                item = itemsList[3],
                itemClick =  {
                    showDialog.value = showNotifications.value ?: true
                }
            )

        }

    }

    if(showDialog.value) {
        PrecipitationLocationsDialog(
            locations = locations,
            selectedLocations = (selectedLocations.value ?: emptySet()) as Set<String>,
            onDismissRequest = { showDialog.value = false },
            onConfirmed = {newLocations ->
                coroutineScope.launch {
                    preferencesRepository.savePrecipitationLocations(newLocations)
                    showDialog.value = false
                }
            },
            coroutineScope = coroutineScope
        )
    }

}


@Composable
fun PrecipitationLocationsDialog(
    locations: List<String>,
    selectedLocations: Set<String>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmed: (Set<String>) -> Unit,
    coroutineScope: CoroutineScope
) {

    val newlySelectedLocations = rememberSaveable { mutableStateOf(selectedLocations) }
    val newLocations = selectedLocations.toMutableSet()
    AlertDialog(
        title = {
            Text(
                text = "Locations",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                locations.forEach { location ->
                    LabeledCheckBox(
                        text = location,
                        checked = newlySelectedLocations.value.contains(location),
                        onCheckedChanged = {
                            if(it) newLocations.add(location) else newLocations.remove(location)

                            newlySelectedLocations.value = newLocations
                        }
                    )
                }
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmed(newlySelectedLocations.value)
            }) {
                Text(text = "Ok")
            }
        },
        shape = RoundedCornerShape(size = 4.dp),
        modifier = modifier
    )

}
@Composable
private fun prepareNotificationSettings(): List<SettingsDrawerItem> {
    val itemsList = mutableListOf<SettingsDrawerItem>()

    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(R.drawable.ic_baseline_notifications_24),
            label = "Show Notifications?"
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_location_24),
            label = "Show Local Forecast?"
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(R.drawable.ic_rain_svgrepo_com),
            label = "Precipitation Notifications?"
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_menu_24),
            label = "Select Precipitation Notifications"
        )
    )
    return itemsList
}




