package com.brian.weathercompose.presentation.screens.settings


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
import com.brian.weathercompose.presentation.screens.reusablecomposables.SettingsListItemWithSwitch
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
    val selectedLocations = preferencesRepository.getPrecipitationLocations.collectAsState(null)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {

        item {
            SettingsListItemWithSwitch(
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
            SettingsListItemWithSwitch(
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
            SettingsListItemWithSwitch(
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
            selectedLocations = ((selectedLocations.value?.toMutableList() ?: emptyList() )),
            onDismissRequest = { showDialog.value = false },
            onConfirmed = {newLocations ->
                coroutineScope.launch {
                    preferencesRepository.savePrecipitationLocations(newLocations)
                    showDialog.value = false
                }
            },
            coroutineScope = coroutineScope,
            preferencesRepository = preferencesRepository
        )
    }

}


@Composable
fun PrecipitationLocationsDialog(
    locations: List<String>,
    selectedLocations: List<String>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmed: (Set<String>) -> Unit,
    coroutineScope: CoroutineScope,
    preferencesRepository: PreferencesRepository
) {

    val newlySelectedLocations = remember(Unit) { mutableStateOf(selectedLocations.toMutableList()) }
    println(newlySelectedLocations.value)

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
                            if(it) newlySelectedLocations.value.add(location) else newlySelectedLocations.value.remove(location)
                            println("New Locations: ${newlySelectedLocations.value}")
                            //newlySelectedLocations.value = newLocations
                            coroutineScope.launch {
                                preferencesRepository.savePrecipitationLocations(newlySelectedLocations.value.toSet())
                            }
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
                onConfirmed(newlySelectedLocations.value.toSet())
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




