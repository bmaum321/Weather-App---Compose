package com.brian.weather.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
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
import com.brian.weather.presentation.SettingsDrawerItem
import com.brian.weather.presentation.SettingsListItem
import com.brian.weather.presentation.reusablecomposables.LabeledRadioButton
import com.brian.weather.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun UnitSettingsScreen(
    openTemperatureDialog: MutableState<Boolean>,
    openClockFormatDialog: MutableState<Boolean>,
    openDateFormatDialog: MutableState<Boolean>,
    openWindspeedDialog: MutableState<Boolean>,
    openMeasurementDialog: MutableState<Boolean>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    itemClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
    coroutineScope: CoroutineScope,
    preferencesRepository: PreferencesRepository,
) {
    viewModel.updateActionBarTitle("Units")
    val itemsList = prepareUnitSettings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {

        items(itemsList) { item ->
            SettingsListItem(
                item = item,
                itemClick = { itemClick(item.label) }
            )

        }
    }

    val temperatureUnit = preferencesRepository.getTemperatureUnit.collectAsState(initial = "")
    if (openTemperatureDialog.value) {
        TemperatureUnitDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            initialSelectedOption = temperatureUnit.value ?: "",
            optionNames = listOf("Fahrenheit", "Celsius"),
            onConfirmed = { selectedOption ->
                coroutineScope.launch {
                    preferencesRepository.saveTemperatureSetting(selectedOption)
                    openTemperatureDialog.value = false
                }
            }
        )
    }


    val windSpeedUnit = preferencesRepository.getWindspeedUnit.collectAsState(initial = "")
    if (openWindspeedDialog.value) {
        WindSpeedUnitDialog(
            modifier = modifier,
            onDismissRequest = { openWindspeedDialog.value = false },
            initialSelectedOption = windSpeedUnit.value ?: "",
            optionNames = listOf("MPH", "KPH"),
            onConfirmed = { selectedOption ->
                coroutineScope.launch {
                    preferencesRepository.saveWindspeedSetting(selectedOption)
                    openWindspeedDialog.value = false
                }
            }
        )
    }

    val measurementUnit = preferencesRepository.getMeasurementUnit.collectAsState(initial = "")
    if (openMeasurementDialog.value) {
        MeasurementUnitDialog(
            modifier = modifier,
            onDismissRequest = { openMeasurementDialog.value = false },
            initialSelectedOption = measurementUnit.value ?: "",
            optionNames = listOf("IN", "MM"),
            onConfirmed = { selectedOption ->
                coroutineScope.launch {
                    preferencesRepository.saveMeasurementSetting(selectedOption)
                    openMeasurementDialog.value = false
                }
            }
        )
    }

    val clockFormat = preferencesRepository.getClockFormat.collectAsState(initial = "")
    if(openClockFormatDialog.value) {
        ClockFormatDialog(
            optionNames = listOf(
                Pair("12 hour", stringResource(R.string.twelve_hour_clock_format)),
                Pair("24 hour", stringResource(R.string.twenty_four_hour_clock_format)) ),
            initialSelectedOption = clockFormat.value ?: "",
            onDismissRequest = { openClockFormatDialog.value = false }
        ) { selectedOption ->
            coroutineScope.launch {
                preferencesRepository.saveClockFormatSetting(selectedOption)
                openClockFormatDialog.value = false
            }
        }

    }

    val dateFormat = preferencesRepository.getDateFormat.collectAsState(initial = "")
    if(openDateFormatDialog.value) {
        DateFormatDialog(
            optionNames = listOf(
                Pair("MM/DD", "MM/DD"),
                Pair("DD/MM", "DD/MM") ),
            initialSelectedOption = dateFormat.value ?: "",
            onDismissRequest = { openDateFormatDialog.value = false }
        ) { selectedOption ->
            coroutineScope.launch {
                preferencesRepository.saveDateFormatSetting(selectedOption)
                openDateFormatDialog.value = false
            }
        }

    }
}

@Composable
private fun prepareUnitSettings(): List<SettingsDrawerItem> {
    val itemsList = mutableListOf<SettingsDrawerItem>()

    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(R.drawable.ic_temperature),
            label = "Temperature"
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.barometer_svgrepo_com),
            label = "Precipitation / Pressure",
            showUnreadBubble = false
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_wind),
            label = "Wind",
            showUnreadBubble = false
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_access_time_24),
            label = "Clock Format"
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_calendar_month_24),
            label = "Date Format"
        )
    )
    return itemsList
}

@Composable
fun TemperatureUnitDialog(
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
                text = "Temperature Unit",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                optionNames.forEach { optionName ->
                    LabeledRadioButton(
                        selected = optionName == selectedOption.value,
                        onClick = { selectedOption.value = optionName } ,
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

@Composable
fun WindSpeedUnitDialog(
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
                text = "Wind Speed Unit",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                optionNames.forEach { optionName ->
                    LabeledRadioButton(
                        selected = optionName == selectedOption.value,
                        onClick = { selectedOption.value = optionName } ,
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

@Composable
fun MeasurementUnitDialog(
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
                text = "Measurement Unit",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                optionNames.forEach { optionName ->
                    LabeledRadioButton(
                        selected = optionName == selectedOption.value,
                        onClick = { selectedOption.value = optionName } ,
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


@Composable
fun ClockFormatDialog(
    optionNames: List<Pair<String, String>>,
    initialSelectedOption: String,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmed: (String) -> Unit
) {

    val selectedOption = rememberSaveable { mutableStateOf(initialSelectedOption) }
    AlertDialog(
        title = {
            Text(
                text = "Clock Format",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                optionNames.forEach { optionName ->
                    LabeledRadioButton(
                        selected = optionName.second == selectedOption.value,
                        onClick = { selectedOption.value = optionName.second } ,
                        text = optionName.first
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

@Composable
fun DateFormatDialog(
    optionNames: List<Pair<String, String>>,
    initialSelectedOption: String,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmed: (String) -> Unit
) {

    val selectedOption = rememberSaveable { mutableStateOf(initialSelectedOption) }
    AlertDialog(
        title = {
            Text(
                text = "Date Format",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                optionNames.forEach { optionName ->
                    LabeledRadioButton(
                        selected = optionName.second == selectedOption.value,
                        onClick = { selectedOption.value = optionName.second } ,
                        text = optionName.first
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
