package com.brian.weathercompose.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.brian.weathercompose.R
import com.brian.weathercompose.data.remote.dto.Day
import com.brian.weathercompose.domain.model.DayDomainObject
import com.brian.weathercompose.domain.model.ForecastDomainObject
import com.brian.weathercompose.presentation.screens.reusablecomposables.ErrorScreen
import com.brian.weathercompose.presentation.screens.reusablecomposables.LoadingScreen
import com.brian.weathercompose.presentation.screens.reusablecomposables.WeatherConditionIcon
import com.brian.weathercompose.presentation.viewmodels.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel


@Composable
fun DailyForecastScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    location: String,
    mainViewModel: MainViewModel,
    alertFabOnClick: () -> Unit
) {
    val dailyForecastViewModel = getViewModel<DailyForecastViewModel>()
    // update title bar

    // This only seems to work if I pass the viewmodel all the way down from main activity and only have one instance of main view model, grabbing it from Koin doesnt work
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            mainViewModel.updateActionBarTitle(dailyForecastViewModel.getWeatherByZipcode(location).cityName)
        }

    }
    val context = LocalContext.current
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val state by remember {dailyForecastViewModel.getForecastForZipcode(location, pref, context.resources) }.collectAsState()

    when (state) {
        is ForecastViewData.Loading -> LoadingScreen(modifier)
        is ForecastViewData.Done -> ForecastList(
            (state as ForecastViewData.Done).forecastDomainObject,
            modifier,
            onClick,
            dailyForecastViewModel,
            alertFabOnClick
        )
        is ForecastViewData.Error -> ErrorScreen({ dailyForecastViewModel.refresh() }, modifier)
    }
}


/**
 * Screen displaying Daily Forecast
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ForecastList(
    forecast: ForecastDomainObject,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    viewModel: DailyForecastViewModel,
    alertFabOnClick: () -> Unit

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
    val scaffoldState = rememberScaffoldState()
    val fabVisible by remember { mutableStateOf(forecast.alerts.isNotEmpty()) }
    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            if(fabVisible) {
                AlertFab(
                    onClick = alertFabOnClick
                )
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.pullRefresh(state)) {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(forecast.days) {
                    ForecastListItem(it, onClick = onClick)
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = state,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }


}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ForecastListItem(
    day: DayDomainObject,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    val date = day.date
    Card(
        modifier = Modifier
            .padding(8.dp)
            .height(100.dp),
        elevation = 4.dp,
        onClick = { onClick(date) }
    ) {
        Box(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.Center)
            ) {
                Column(modifier = modifier.weight(3f)) {
                    Text(
                        text = day.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = day.day.condition.text,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.weight(.5f))

                Row(
                    modifier = modifier.padding(top=10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${day.day.mintemp_f.toInt()}\u00B0 \\",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "  ${day.day.maxtemp_f.toInt()}\u00B0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )


                }
                Spacer(modifier = Modifier.weight(1f))
                WeatherConditionIcon(iconUrl = day.day.condition.icon)
            }
        }


    }
}

// FAB for add weather
@Composable
fun AlertFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(size = 18.dp),
        modifier = modifier.size(64.dp),
        backgroundColor = Color.Red
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_crisis_alert_24),
            contentDescription = stringResource(R.string.alert_fab_description)
        )

    }
}


