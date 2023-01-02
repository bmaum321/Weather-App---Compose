package com.brian.weather.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brian.weather.R
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.presentation.screens.SettingsDrawerItem
import com.brian.weather.presentation.reusablecomposables.SettingsListItemWithSwitch
import com.brian.weather.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun InterfaceSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    preferencesRepository: PreferencesRepository,
) {
    viewModel.updateActionBarTitle("Interface Settings")
    val itemsList = prepareInterfaceSettings()

    val dynamicColorsSetting = preferencesRepository.getDynamicColorsSetting.collectAsState(initial = null)
    val showAlertsSetting = preferencesRepository.getWeatherAlertsSetting.collectAsState(initial = null)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {

       item {
           SettingsListItemWithSwitch(
               item = itemsList[0],
               isChecked = dynamicColorsSetting.value ?: true,
               onCheckedChanged = {
                   coroutineScope.launch {
                       preferencesRepository.saveDynamicColorSetting(!dynamicColorsSetting.value!!)
                   }
               }
           )
       }

        item {
            SettingsListItemWithSwitch(
                item = itemsList[1],
                isChecked = showAlertsSetting.value ?: true,
                onCheckedChanged = {
                    coroutineScope.launch {
                        preferencesRepository.saveWeatherAlertSetting(!showAlertsSetting.value!!)
                    }
                }
            )
        }
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
    return itemsList
}




