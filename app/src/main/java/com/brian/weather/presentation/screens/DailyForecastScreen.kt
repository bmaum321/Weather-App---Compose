package com.brian.weather.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weather.R
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.domain.model.DaysDomainObject
import com.brian.weather.domain.model.ForecastDomainObject
import com.brian.weather.presentation.animations.Pulsating
import com.brian.weather.presentation.animations.pressClickEffect
import com.brian.weather.presentation.reusablecomposables.ErrorScreen
import com.brian.weather.presentation.reusablecomposables.LoadingScreen
import com.brian.weather.presentation.reusablecomposables.MarqueeText
import com.brian.weather.presentation.reusablecomposables.WeatherConditionIcon
import com.brian.weather.presentation.viewmodels.*
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


@Composable
fun DailyForecastScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    alertFabOnClick: () -> Unit,
    dailyForecastViewModel: DailyForecastViewModel,
    preferences: AppPreferences,
    state: ForecastState,
    retryAction: () -> Unit
) {
    when (state) {
        is ForecastState.Loading -> LoadingScreen(modifier)
        is ForecastState.Success -> {
            ForecastList(
                state.forecastDomainObject,
                modifier,
                onClick,
                dailyForecastViewModel,
                alertFabOnClick,
                preferences
            )
        }
        is ForecastState.Error -> ErrorScreen( retryAction, modifier)
    }
}


/**
 * Screen displaying Daily Forecast
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ForecastList(
    forecast: ForecastDomainObject,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    viewModel: DailyForecastViewModel,
    alertFabOnClick: () -> Unit,
    preferences: AppPreferences
) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        viewModel.refresh()
        refreshing = false
    }

    // val state = rememberPullRefreshState(
    //     refreshing = refreshing,
    //      onRefresh = { refresh() }
//    )
    val listState = rememberLazyListState()
    val showAlertFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    val fabVisible by remember { mutableStateOf(forecast.alerts.isNotEmpty() && preferences.showAlerts) }
    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = showAlertFab,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                if (fabVisible) {
                    Pulsating {
                        AlertFab(onClick = alertFabOnClick)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        Box {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = innerPadding,
                state = listState
            ) {
                items(forecast.days) {
                    ForecastListItem(
                        it,
                        onClick = onClick,
                        gradientColors = it.day.backgroundColors,
                        viewModel = viewModel,
                        preferences = preferences
                    )
                }
            }
            // PullRefreshIndicator(
            //      refreshing = refreshing,
            //       state = state,
            //      Modifier.align(Alignment.TopCenter)
            //  )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ForecastListItem(
    daysDomainObject: DaysDomainObject,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    gradientColors: List<Color>,
    viewModel: DailyForecastViewModel,
    preferences: AppPreferences
) {

    val ticker = viewModel.dailyForecastTicker(
        chanceOfRain = daysDomainObject.day.daily_chance_of_rain,
        chanceOfSnow = daysDomainObject.day.daily_chance_of_snow,
        avgTemp = daysDomainObject.day.avgTemp,
        sunrise = daysDomainObject.astroData.sunrise,
        sunset = daysDomainObject.astroData.sunset,
        avgHumidity = daysDomainObject.day.avgHumidity
    ).collectAsState(initial = "")

    val date = daysDomainObject.dayOfWeek
    val gradient = Brush.linearGradient(gradientColors)
    val colors =
        CardDefaults.cardColors(contentColor = if (preferences.dynamicColors) daysDomainObject.day.textColor else LocalContentColor.current)

    Card(
        modifier = Modifier
            .padding(8.dp)
            .height(125.dp)
            .pressClickEffect(),
        onClick = { onClick(date) },
        colors = colors
        // contentColor = if (dynamicColorsEnabled.value) daysDomainObject.day.textColor else LocalContentColor.current
    ) {
        Box(
            modifier = if (preferences.dynamicColors) modifier
                .background(gradient)
                .fillMaxSize() else modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.Center)
            ) {
                Column(modifier = modifier.weight(6.5f)) {
                    Text(
                        text = daysDomainObject.dayOfWeek,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = daysDomainObject.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    if(daysDomainObject.day.condition.text.length > 13) {
                        /**
                         * This causes UI tests to idle out
                         */
                        MarqueeText(text = daysDomainObject.day.condition.text, fontSize = 18.sp)
                    } else {
                        Text(
                            text = daysDomainObject.day.condition.text,
                            fontSize = 18.sp,
                        )
                    }



                }
                Spacer(modifier = Modifier.weight(.5f))

                Column(modifier = Modifier.weight(8f)) {

                    Row(
                        modifier = modifier.padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text =  "${daysDomainObject.day.minTemp.toInt()}\u00B0 · ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${daysDomainObject.day.maxTemp.toInt()}\u00B0",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                    }

                    AnimatedContent(
                        modifier = Modifier.animateContentSize(),
                        targetState = ticker.value,
                        transitionSpec = {

                            (slideInVertically { height -> height } + fadeIn() with
                                    slideOutVertically { height -> -height } + fadeOut())
                                .using(
                                    // Disable clipping since the faded slide-in/out should
                                    // be displayed out of bounds.
                                    SizeTransform(clip = false)
                                )
                        }
                    ) { targetString ->
                        Text(text = targetString, textAlign = TextAlign.Center)
                    }

                }

                Spacer(modifier = Modifier.weight(.5f))
                WeatherConditionIcon(iconUrl = daysDomainObject.day.condition.icon, iconSize = 64)
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
        modifier = modifier
            .size(64.dp)
            .pressClickEffect(),
        containerColor = Color.Red
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_warning_24),
            contentDescription = stringResource(R.string.alert_fab_description)
        )

    }
}


