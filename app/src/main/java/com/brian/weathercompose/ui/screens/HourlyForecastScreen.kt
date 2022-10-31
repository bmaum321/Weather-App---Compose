package com.brian.weathercompose.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import coil.decode.ImageSource
import com.brian.weathercompose.R
import com.brian.weathercompose.data.BaseApplication
import com.brian.weathercompose.model.Hours
import com.brian.weathercompose.ui.screens.reusablecomposables.ErrorScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.LoadingScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.WeatherConditionIcon
import com.brian.weathercompose.ui.viewmodels.*
import kotlinx.coroutines.launch

@Composable
fun HourlyForecastScreen(
    modifier: Modifier = Modifier,
    location: String,
    date: String
) {
    val application = BaseApplication()
    val viewModel: HourlyForecastViewModel = //TODO needs to be another viewmodel for hourly screen
        viewModel(
            factory = HourlyForecastViewModel
                .HourlyForecastViewModelFactory(
                    application.database.weatherDao(),
                    application
                )
        )
    val context = LocalContext.current
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    LaunchedEffect(key1 = true) {
        // should this ui state be a private set so only the viewmodel can change the state?
        viewModel.getHourlyForecast(location, pref, context.resources)
            .collect { //need to pass zipcode from nav args here
                when (it) {
                    is HourlyForecastViewData.Done -> viewModel.hourlyForecastUiState =
                        HourlyForecastViewData.Done(it.forecastDomainObject)
                    is HourlyForecastViewData.Loading -> viewModel.hourlyForecastUiState =
                        HourlyForecastViewData.Loading
                    is HourlyForecastViewData.Error -> viewModel.hourlyForecastUiState =
                        HourlyForecastViewData.Error(it.code, it.message)

                }
            }

    }

    when (viewModel.hourlyForecastUiState) {  //TODO same here new viewmodel with new state
        is HourlyForecastViewData.Loading -> LoadingScreen(modifier)
        is HourlyForecastViewData.Done -> HourlyForecastList(
            (viewModel.hourlyForecastUiState as HourlyForecastViewData.Done).forecastDomainObject.days.first { it.date == date }.hour, //TODO need to pass a list of days here
            modifier,
            viewModel
        )
        is HourlyForecastViewData.Error -> ErrorScreen({ viewModel.refresh() }, modifier)
    }
}


/**
 * Screen displaying Daily Forecast
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HourlyForecastList(
    hoursList: List<Hours>,
    modifier: Modifier = Modifier,
    viewModel: HourlyForecastViewModel
) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        viewModel.refresh()
        refreshing = false
    }

    val state = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refresh() }
    )

    Box(modifier = Modifier.pullRefresh(state)) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(hoursList) {
                HourlyForecastListItem(it)
            }
        }
        PullRefreshIndicator(
            refreshing = refreshing,
            state = state,
            Modifier.align(Alignment.TopCenter)
        )
    }


}


@Composable
fun HourlyForecastListItem(
    hour: Hours,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = modifier.weight(3f)) {
                    Text(
                        text = hour.time,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = hour.condition.text,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.weight(.5f))

                Row {
                    Text(
                        text = "${hour.temp_f.toInt()}\u00B0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )


                }
                Spacer(modifier = Modifier.weight(1f))
                WeatherConditionIcon(iconUrl = hour.condition.icon)
                ExpandCardButton(expanded = expanded, onClick = { expanded = !expanded })
            }

            if(expanded){
                HourlyForecastDetails(hour = hour)
            }
        }
    }
    
}

@Composable
fun HourlyForecastDetails(
    hour: Hours,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(
            start = 16.dp,
            top = 8.dp,
            bottom = 16.dp,
            end = 16.dp
        )
            .fillMaxWidth()
    ) {
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(iconId = R.drawable.ic_wind, value = hour.wind_mph.toString())
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(iconId = R.drawable.barometer_svgrepo_com, value = hour.pressure_in.toString())
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(iconId = R.drawable.ic_rain_svgrepo_com, value = hour.precip_in.toString())
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(iconId = R.drawable.ic_wind_sock, value = hour.wind_dir)
        Spacer(modifier = modifier.weight(1f))
    }
}

/**
 * Composable that displays a button that is clickable and displays an expand more or an expand less
 * icon.
 *
 * @param expanded represents whether the expand more or expand less icon is visible
 * @param onClick is the action that happens when the button is clicked
 * @param modifier modifiers to set to this composable
 */
@Composable
private fun ExpandCardButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = if (expanded) painterResource(id = R.drawable.ic_baseline_expand_less_24) else painterResource(
                id = R.drawable.ic_baseline_expand_more_24
            ),
            contentDescription = stringResource(R.string.expand_button_content_description),
        )
    }
}

@Composable
private fun WeatherStatistic(
    @DrawableRes iconId: Int,
    value: String,
    modifier: Modifier = Modifier

) {
    Column(modifier = modifier.wrapContentSize(Alignment.Center)) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = iconId.toString()
        )

        Text(text = value)
    }
}


