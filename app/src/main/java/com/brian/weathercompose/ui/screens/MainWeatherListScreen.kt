package com.brian.weathercompose.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.brian.weathercompose.R
import com.brian.weathercompose.domain.WeatherDomainObject
import com.brian.weathercompose.ui.screens.reusablecomposables.ErrorScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.LoadingScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.WeatherConditionIcon
import com.brian.weathercompose.ui.theme.WeatherComposeTheme
import com.brian.weathercompose.ui.viewmodels.MainViewModel
import com.brian.weathercompose.ui.viewmodels.WeatherListState
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel
import kotlinx.coroutines.*
import org.koin.androidx.compose.getViewModel

@Composable
fun MainWeatherListScreen(
    weatherUiState: WeatherListState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    addWeatherFabAction: () -> Unit,
    weatherListViewModel: WeatherListViewModel, //Should I be passing around a viewmodel like this to subcomposables?
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
        is WeatherListState.Error -> ErrorScreen(retryAction, modifier)
    }
}


/**
 * The home screen displaying list of weather objects
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalUnitApi::class)
@Composable
fun WeatherListScreen(
    weatherDomainObjectList: List<WeatherDomainObject>,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    addWeatherFabAction: () -> Unit,
    weatherListViewModel: WeatherListViewModel,
) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        weatherListViewModel.refresh()
        refreshing = false
    }

    val refreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refresh() }
    )

    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            AddWeatherFab(
                onClick = addWeatherFabAction
            )
        }
    ) { innerPadding ->

        Box(modifier = Modifier.pullRefresh(refreshState)) {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(4.dp),
                state = listState
            ) {
                items(weatherDomainObjectList) { item ->
                    // WeatherListItem(item, onClick = onClick)
                    val dismissState = rememberDismissState()
                    var placeDeleted = ""
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
                                placeDeleted = weatherEntity.cityName
                            }
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
                            val alignment = Alignment.CenterEnd
                            val icon = Icons.Default.Delete
                            val scale by animateFloatAsState(
                                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                            )

                            Card(
                                Modifier
                                    .padding(8.dp)
                                    .height(100.dp)
                                    .fillMaxWidth(),
                                backgroundColor = color
                            ) {
                                Box(
                                    contentAlignment = alignment
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = "Delete Icon",
                                        modifier = Modifier.scale(scale)
                                    )
                                }
                            }
                        },

                        dismissContent = {
                            WeatherListItem(weatherDomainObject = item, onClick = onClick)
                        }

                    )

                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = refreshState,
                Modifier.align(Alignment.TopCenter)
            )

            AnimatedVisibility(visible = showButton) {
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Animated Button")
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
        modifier = modifier.size(64.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add_24),
            contentDescription = stringResource(R.string.add_weather_fab_description)
        )

    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeatherListItem(
    weatherDomainObject: WeatherDomainObject,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    val location = weatherDomainObject.zipcode
    Card(
        modifier = Modifier
            .padding(8.dp)
            .height(100.dp)
            .fillMaxWidth(),
        elevation = 4.dp,
        onClick = { onClick(location) },
        // backgroundColor = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = modifier.weight(3f)) {
                Text(
                    text = weatherDomainObject.location,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(text = weatherDomainObject.country)
                Text(
                    text = weatherDomainObject.conditionText,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Column {
                Text(
                    text = "${weatherDomainObject.temp}\u00B0",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = weatherDomainObject.time)
            }
            Spacer(modifier = Modifier.weight(1f))
            WeatherConditionIcon(iconUrl = weatherDomainObject.imgSrcUrl)
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
                "SSW", "", 1, 1000, 1, "USA", "32"
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
