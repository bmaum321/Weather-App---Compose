package com.brian.weathercompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.brian.weathercompose.data.BaseApplication
import com.brian.weathercompose.model.Day
import com.brian.weathercompose.ui.screens.reusablecomposables.ErrorScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.LoadingScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.WeatherConditionIcon
import com.brian.weathercompose.ui.viewmodels.*
import kotlinx.coroutines.launch


@Composable
fun DailyForecastScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    location: String
) {
    val application = BaseApplication()
    val viewModel: DailyForecastViewModel =
        viewModel(
            factory = DailyForecastViewModel
                .DailyForecastViewModelFactory(
                    application.database.weatherDao(),
                    application
                )
        )
    val mainViewModel: MainViewModel = viewModel()
    // update title bar
    LaunchedEffect(key1 = true) {
        mainViewModel.updateActionBarTitle(location)
    }
    val context = LocalContext.current
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val state by remember {viewModel.getForecastForZipcode(location, pref, context.resources) }.collectAsState()

    when (state) {
        is ForecastViewData.Loading -> LoadingScreen(modifier)
        is ForecastViewData.Done -> ForecastList(
            (state as ForecastViewData.Done).forecastDomainObject.days, //TODO need to pass a list of days here
            modifier,
            onClick,
            viewModel
        )
        is ForecastViewData.Error -> ErrorScreen({ viewModel.refresh() }, modifier)
    }
}


/**
 * Screen displaying Daily Forecast
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ForecastList(
    dayList: List<Day>,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    viewModel: DailyForecastViewModel
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
            items(dayList) {
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ForecastListItem(
    day: Day,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    val date = day.date
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = 4.dp,
        onClick = { onClick(date) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
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

            Row {
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


