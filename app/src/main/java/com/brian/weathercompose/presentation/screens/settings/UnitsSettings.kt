package com.brian.weathercompose.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weathercompose.R
import com.brian.weathercompose.data.settings.SettingsRepositoryImpl
import com.brian.weathercompose.presentation.SettingsDrawerItem
import com.brian.weathercompose.presentation.SettingsListItem
import com.brian.weathercompose.presentation.screens.reusablecomposables.LabeledRadioButton
import com.brian.weathercompose.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get


@Composable
fun UnitSettingsScreen(
    openTemperatureDialog: MutableState<Boolean>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    itemClick: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val datastore = SettingsRepositoryImpl(get())
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

    val temperatureUnit = datastore.getTemperatureUnit.collectAsState(initial = "")
    println("Latest temp unit in preferences is $temperatureUnit")

    if (openTemperatureDialog.value) {
        TemperatureUnitDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            initialSelectedOption = temperatureUnit.value ?: "",
            optionNames = listOf("Fahrenheit", "Celsius"),
            onConfirmed = { selectedOption ->
                coroutineScope.launch {
                    datastore.saveTempSetting(selectedOption)
                    openTemperatureDialog.value = false
                }
            }
        )
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
            label = "Pressure",
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
