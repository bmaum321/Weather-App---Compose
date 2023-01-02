package com.brian.weather.presentation.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weather.R
import com.brian.weather.domain.model.HoursDomainObject
import com.brian.weather.presentation.animations.pressClickEffect
import com.brian.weather.presentation.reusablecomposables.ErrorScreen
import com.brian.weather.presentation.reusablecomposables.LoadingScreen
import com.brian.weather.presentation.reusablecomposables.MarqueeText
import com.brian.weather.presentation.reusablecomposables.WeatherConditionIcon
import com.brian.weather.presentation.theme.WeatherComposeTheme
import com.brian.weather.presentation.viewmodels.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun HourlyForecastScreen(
    modifier: Modifier = Modifier,
    location: String,
    date: String,
    mainViewModel: MainViewModel,
    hourlyForecastViewModel: HourlyForecastViewModel
) {
    //val hourlyForecastViewModel = getViewModel<HourlyForecastViewModel>()
    // update title bar
    LaunchedEffect(Unit) {
        mainViewModel.updateActionBarTitle(date)
    }
    val context = LocalContext.current
    val uiState = remember {
        hourlyForecastViewModel.getHourlyForecast(
            location,
            context.resources
        )
    }.collectAsState()

    when (uiState.value) {  //TODO same here new viewmodel with new state
        is HourlyForecastViewData.Loading -> LoadingScreen(modifier)
        is HourlyForecastViewData.Done -> HourlyForecastList(
            (uiState.value as HourlyForecastViewData.Done).forecastDomainObject.days.first { it.dayOfWeek == date }.hours,
            modifier,
            hourlyForecastViewModel
        )
        is HourlyForecastViewData.Error -> ErrorScreen(
            { hourlyForecastViewModel.refresh() },
            modifier
        )
    }
}


/**
 * Screen displaying Daily Forecast
 */

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HourlyForecastList(
    hoursList: List<HoursDomainObject>,
    modifier: Modifier = Modifier,
    viewModel: HourlyForecastViewModel,
) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        viewModel.refresh()
        refreshing = false
    }

    val listState = rememberLazyListState()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val coroutineScope = rememberCoroutineScope()

    // val state = rememberPullRefreshState(
    //    refreshing = refreshing,
    //     onRefresh = { refresh() }
    //  )

    //  Box(modifier = Modifier.pullRefresh(state)) {
    Box {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(4.dp),
            state = listState
        ) {
            items(hoursList) { hourDomainObject ->
                HourlyForecastListItem(
                    hourDomainObject,
                    temperatureUnit = viewModel.getTempUnit(),
                    windUnit = viewModel.getWindUnit(),
                    measurementUnit = viewModel.getMeasurement(),
                    colors = hourDomainObject.colors,
                    dynamicColorsEnabled = viewModel.getDynamicColorSetting()
                )
            }
        }

        AnimatedVisibility(
            visible = showButton,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0, 0)
                    }
                },
                modifier = Modifier
                    .size(32.dp)
                    .pressClickEffect()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_expand_less_24),
                    contentDescription = stringResource(R.string.scroll_to_top)
                )
            }
        }
        // PullRefreshIndicator(
        //    refreshing = refreshing,
        //      state = state,
        //      Modifier.align(Alignment.TopCenter)
        //   )
    }
}


@Composable
fun HourlyForecastListItem(
    hour: HoursDomainObject,
    modifier: Modifier = Modifier,
    temperatureUnit: String,
    windUnit: String,
    measurementUnit: String,
    colors: List<Color>,
    dynamicColorsEnabled: Boolean
) {

    val gradient = Brush.linearGradient(colors)
    val colors =
        CardDefaults.cardColors(contentColor = if (dynamicColorsEnabled) hour.textColor else LocalContentColor.current)
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .padding(8.dp),
        colors = colors
    ) {
        Box(
            modifier = if (dynamicColorsEnabled) modifier
                .background(gradient)
                .fillMaxSize() else modifier.fillMaxSize()
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
                        if(hour.condition.text.length > 13) {
                            MarqueeText(text = hour.condition.text, fontSize = 18.sp)
                        } else {
                            Text(
                                text = hour.condition.text,
                                fontSize = 18.sp
                            )
                        }

                    }
                    Spacer(modifier = Modifier.weight(.5f))

                    Row {
                        Text(
                            text = "${hour.temp}\u00B0",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )


                    }
                    Spacer(modifier = Modifier.weight(1f))
                    WeatherConditionIcon(iconUrl = hour.condition.icon, iconSize = 64)
                    ExpandCardButton(expanded = expanded, onClick = { expanded = !expanded })
                }

                if (expanded) {
                    HourlyForecastDetails(
                        hour = hour,
                        windUnit = windUnit,
                        measurementUnit = measurementUnit
                    )
                }
            }
        }
    }

}

@Composable
fun HourlyForecastDetails(
    hour: HoursDomainObject,
    modifier: Modifier = Modifier,
    windUnit: String,
    measurementUnit: String

) {
    Row(
        modifier = modifier
            .padding(
                start = 16.dp,
                top = 8.dp,
                bottom = 16.dp,
                end = 16.dp
            )
            .fillMaxWidth()
    ) {
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(
            iconId = R.drawable.ic_wind,
            value = if (windUnit == "MPH") hour.windspeed + " MPH" else hour.windspeed + " KPH",
            modifier = Modifier.semantics { testTag = windUnit }
        )
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(
            iconId = R.drawable.barometer_svgrepo_com,
            value = if (measurementUnit == "IN") hour.pressure + " IN" else hour.pressure + " MB",
            modifier = Modifier.semantics { testTag = measurementUnit }
        )
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(
            iconId = R.drawable.ic_rain_svgrepo_com,
            value = if (measurementUnit == "IN") hour.precip + " IN" else hour.precip + " MM",
            modifier = Modifier.semantics { testTag = measurementUnit }
        )
        Spacer(modifier = modifier.weight(1f))
        WeatherStatistic(
            iconId = R.drawable.ic_wind_sock,
            value = hour.wind_dir
        )
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
            painter = if (expanded) painterResource(id = R.drawable.ic_baseline_expand_less_24)
            else painterResource(id = R.drawable.ic_baseline_expand_more_24),
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.wrapContentSize(Alignment.Center)
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = iconId.toString()
        )

        Text(text = value)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun WeatherStatisticPreview() {
    WeatherComposeTheme {
        WeatherStatistic(iconId = R.drawable.barometer_svgrepo_com, value = "12 MPH")
    }
}

