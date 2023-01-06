package com.brian.weather.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weather.R
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.presentation.reusablecomposables.LabeledRadioButton
import com.brian.weather.presentation.screens.SettingsDrawerItem
import com.brian.weather.presentation.reusablecomposables.SettingsListItemWithSwitch
import com.brian.weather.presentation.screens.SettingsListItem
import com.brian.weather.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun InterfaceSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    preferencesRepository: PreferencesRepository,
    openSizeSelectorDialog: MutableState<Boolean>
) {
    viewModel.updateActionBarTitle("Interface Settings")
    val itemsList = prepareInterfaceSettings()

    val dynamicColorsSetting =
        preferencesRepository.getDynamicColorsSetting.collectAsState(initial = null)
    val showAlertsSetting =
        preferencesRepository.getWeatherAlertsSetting.collectAsState(initial = null)
    val preferences = preferencesRepository.getAllPreferences.collectAsState(initial = null)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {

        item {
            SettingsListItemWithSwitch(
                item = itemsList[0],
                isChecked = preferences.value?.dynamicColors ?: true,
                onCheckedChanged = {
                    coroutineScope.launch {
                        preferencesRepository.saveDynamicColorSetting(!preferences.value?.dynamicColors!!)
                    }
                }
            )
        }

        item {
            SettingsListItemWithSwitch(
                item = itemsList[1],
                isChecked = preferences.value?.showAlerts ?: true,
                onCheckedChanged = {
                    coroutineScope.launch {
                        preferencesRepository.saveWeatherAlertSetting(!preferences.value?.showAlerts!!)
                    }
                }
            )
        }

        item {
            SettingsListItem(
                item = itemsList[2],
                itemClick = { openSizeSelectorDialog.value = true }
            )


        }

    }

    if (openSizeSelectorDialog.value) {
        CardSizeSelector(
            optionNames = listOf("Small", "Medium", "Large"),
            initialSelectedOption = preferences.value?.cardSize ?: "Medium",
            onDismissRequest = { openSizeSelectorDialog.value = false },
            onConfirmed = { selectedOption ->
                coroutineScope.launch {
                    preferencesRepository.saveCardSizeSetting(selectedOption)
                    openSizeSelectorDialog.value = false
                }
            }
        )
    }

}

@Composable
private fun prepareInterfaceSettings(): List<SettingsDrawerItem> {
    val itemsList = mutableListOf<SettingsDrawerItem>()

    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(R.drawable.ic_baseline_color_lens_24),
            label = stringResource(R.string.dynamic_condition_colors)
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_warning_24),
            label = stringResource(R.string.show_weather_alerts),
            showUnreadBubble = false
        )
    )

    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_photo_size_select_small_24),
            label = stringResource(R.string.card_size),
            showUnreadBubble = false
        )
    )
    return itemsList
}


@Composable
fun CardSizeSelector(
    optionNames: List<String>,
    initialSelectedOption: String,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmed: (String) -> Unit
) {

    val selectedOption = rememberSaveable { mutableStateOf(initialSelectedOption) }
    AlertDialog(
        title = {
            Text(
                text = "Weather Card Size",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                optionNames.forEach { optionName ->
                    LabeledRadioButton(
                        selected = optionName == selectedOption.value,
                        onClick = { selectedOption.value = optionName },
                        text = optionName
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
                onConfirmed(selectedOption.value)
            }) {
                Text(text = "Ok")
            }
        },
        shape = RoundedCornerShape(size = 4.dp),
        modifier = modifier
    )

}



