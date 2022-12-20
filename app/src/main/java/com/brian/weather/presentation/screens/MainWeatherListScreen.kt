package com.brian.weather.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.brian.weather.R
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.presentation.animations.Pulsating
import com.brian.weather.presentation.animations.bounceClick
import com.brian.weather.presentation.animations.pressClickEffect
import com.brian.weather.presentation.animations.tickerAnimation
import com.brian.weather.presentation.reusablecomposables.ErrorScreen
import com.brian.weather.presentation.reusablecomposables.LoadingScreen
import com.brian.weather.presentation.reusablecomposables.WeatherConditionIcon
import com.brian.weather.presentation.theme.WeatherComposeTheme
import com.brian.weather.presentation.viewmodels.MainViewModel
import com.brian.weather.presentation.viewmodels.WeatherListState
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.getViewModel

@Composable
fun MainWeatherListScreen(
    weatherUiState: WeatherListState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    addWeatherFabAction: () -> Unit,
    weatherListViewModel: WeatherListViewModel,
    mainViewModel: MainViewModel
) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        mainViewModel.updateActionBarTitle(context.getString(R.string.places))
    }
    when (weatherUiState) {
        is WeatherListState.Empty -> WeatherListScreen(
            emptyList(),
            modifier,
            onClick,
            addWeatherFabAction,
            weatherListViewModel,
        )
        is WeatherListState.Loading -> LoadingScreen(modifier)
        is WeatherListState.Success -> WeatherListScreen(
            weatherUiState.weatherDomainObjects,
            modifier,
            onClick,
            addWeatherFabAction,
            weatherListViewModel,
        )
        is WeatherListState.Error -> ErrorScreen(retryAction, modifier, message = weatherUiState.message)
    }
}


/**
 * The home screen displaying list of weather objects
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeatherListScreen(
    weatherDomainObjectList: List<WeatherDomainObject>,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    addWeatherFabAction: () -> Unit,
    weatherListViewModel: WeatherListViewModel,
) {
    /**
     * Currently passing all the preferences down to the composable to add as semantics tags for
     * testing purposes. This would allow me to make the decision what to present here instead
     * of doing it in the mapper
     *
     * There must also be a better way to pass all the permissions instead of each individually
     */
    val preferences = weatherListViewModel.allPreferences.collectAsState().value
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        weatherListViewModel.refresh()
        refreshing = false
    }

     //val refreshState = rememberPullRefreshState(
     //   refreshing = refreshing,
    //    onRefresh = { refresh() }
    // )

    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val showScrollToTopButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showAddWeatherFab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    //val scaffoldState = rememberScaffoldState()
    Scaffold(
        //    scaffoldState = scaffoldState,
        floatingActionButton =
        {
            if (weatherDomainObjectList.isEmpty()) {
                Pulsating {
                    AddWeatherFab(
                        onClick = addWeatherFabAction
                    )
                }

            } else {
                AnimatedVisibility(
                    visible = showAddWeatherFab,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    AddWeatherFab(
                        onClick = addWeatherFabAction
                    )
                }
            }
        }
        ,
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        // Box(modifier = Modifier.pullRefresh(refreshState)) {
        Box() {

            if (weatherDomainObjectList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_list_action_prompt),
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                }
            }
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(4.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(weatherDomainObjectList) { item ->

                    WeatherListItem(
                        weatherDomainObject = item,
                        onClick = onClick,
                        preferences = preferences,
                        viewModel = weatherListViewModel
                    )
                    /*

                    // WeatherListItem(item, onClick = onClick)
                    val dismissState = rememberDismissState()
                    if (dismissState.isDismissed(DismissDirection.EndToStart) ||
                        dismissState.isDismissed(DismissDirection.StartToEnd)
                    ) {
                        LaunchedEffect(Unit) {
                            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                                message = "${item.zipcode} will be deleted",
                                actionLabel = "Cancel",
                                duration = SnackbarDuration.Short
                            )
                            when (snackbarResult) {
                                SnackbarResult.Dismissed -> {}
                                SnackbarResult.ActionPerformed -> {
                                    cancel()
                                    weatherListViewModel.refresh()
                                }
                            }
                            withContext(Dispatchers.IO) {
                                val weatherEntity =
                                    weatherListViewModel.getWeatherByZipcode(item.zipcode)
                                weatherListViewModel.deleteWeather(weatherEntity)
                            }
                        }

                        LaunchedEffect(Unit) {
                            //TODO the logic here needs some work, the list getting passed to the worker is not updated
                            // When a location is deleted from the database
                            weatherListViewModel.updateLocations(weatherListViewModel.getZipCodesFromDatabase().first().toSet())
                        }
                    }

                    SwipeToDismiss(
                        state = dismissState,
                        modifier = Modifier
                            .padding(vertical = Dp(1f)),
                        directions = setOf(
                            DismissDirection.EndToStart,
                            DismissDirection.StartToEnd
                        ),
                        dismissThresholds = { direction ->
                            FractionalThreshold(if (direction == DismissDirection.EndToStart) 0.1f else 0.1f)
                        },

                        background = {
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    DismissValue.Default -> Color.White
                                    else -> Color.Red
                                }
                            )
                            val alignment =
                                if(dismissState.dismissDirection == DismissDirection.EndToStart)
                                    Alignment.CenterEnd
                                 else
                                    Alignment.CenterStart

                            val icon = Icons.Default.Delete
                            val scale by animateFloatAsState(
                                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                            )

                            Card(
                                Modifier
                                    .padding(8.dp)
                                    .height(175.dp)
                                    .fillMaxWidth(),
                                backgroundColor = color
                            ) {
                                Box(
                                    contentAlignment = alignment
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = "Delete Icon",
                                        modifier = Modifier
                                            .scale(scale)
                                            .padding(8.dp)
                                    )
                                }
                            }
                        },

                        dismissContent = {
                            WeatherListItem(
                                weatherDomainObject = item,
                                onClick = onClick,
                                preferences = preferences,
                                viewModel = weatherListViewModel
                            )

                        }

                    )
                    */


                }
            }
            // PullRefreshIndicator(
            //      refreshing = refreshing,
            //     state = refreshState,
            //     Modifier.align(Alignment.TopCenter)
            //   )

            AnimatedVisibility(
                visible = showScrollToTopButton,
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
        }
    }


}

// FAB for add weather
@Composable
fun AddWeatherFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(size = 18.dp),
        modifier = modifier
            .size(64.dp)
            .pressClickEffect()
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.add_weather_fab_description)
        )

    }
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherListItem(
    weatherDomainObject: WeatherDomainObject,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    preferences: AppPreferences?,
    viewModel: WeatherListViewModel
) {
    val ticker = viewModel.weatherTicker(
        humidity = weatherDomainObject.humidity,
        feelsLikeTemp = weatherDomainObject.feelsLikeTemp,
        time = weatherDomainObject.time,
        windspeed = "${weatherDomainObject.windSpeed.toInt()} ${preferences?.windUnit}"
    ).collectAsState(initial = "")
    val gradient = Brush.linearGradient(weatherDomainObject.backgroundColors)

    val colors =
        CardDefaults.cardColors(
            contentColor = if (preferences?.dynamicColors == true)
                weatherDomainObject.textColor
            else LocalContentColor.current
        )
    Card(
        modifier = Modifier
            .padding(8.dp)
            .height(175.dp)
            .fillMaxWidth()
            .pressClickEffect(),
        onClick = { onClick(weatherDomainObject.zipcode) },
        colors = colors
    ) {
        Box(
            modifier = if (preferences?.dynamicColors == true) Modifier
                .background(gradient)
                .fillMaxSize() else modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.Center)
            ) {
                Column(modifier = modifier.weight(10f)) {
                    Text(
                        text = weatherDomainObject.location,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                    Text(
                        text = weatherDomainObject.country,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = weatherDomainObject.conditionText,
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.weight(.1f))

                Column(modifier = Modifier.weight(7.5f)) {
                    Text(
                        text = "${weatherDomainObject.temp}\u00B0",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { testTag = preferences?.tempUnit ?: "" }
                    )

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
                        Text(
                            text = targetString,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.semantics {
                                testTag = preferences?.clockFormat ?: ""
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.size(4.dp))
                WeatherConditionIcon(iconUrl = weatherDomainObject.imgSrcUrl, iconSize = 64)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    WeatherComposeTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    WeatherComposeTheme {
        ErrorScreen({})
    }
}

// TODO how do I show a preview if I need to pass a viewmodel?
@Preview(showBackground = true)
@Composable
fun WeatherListScreenPreview() {
    WeatherComposeTheme {
        val mockData = List(10) {
            WeatherDomainObject(
                "Liverpool", "32", "13088", "", "Sunny", 12.0,
                "SSW", "", emptyList(), 1000, Color.Black, "USA", "32", 1
            )
        }
        WeatherListScreen(
            mockData,
            onClick = {},
            weatherListViewModel = getViewModel<WeatherListViewModel>(),
            modifier = Modifier,
            addWeatherFabAction = {})
    }
}
