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
import com.brian.weathercompose.ui.viewmodels.*
import kotlinx.coroutines.launch

@Composable
fun ForecastScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
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
    val context = LocalContext.current
    val pref = PreferenceManager.getDefaultSharedPreferences(context)

    LaunchedEffect(key1 = true) {
        viewModel.getForecastForZipcode("13088",pref, context.resources).collect{ //need to pass zipcode from nav args here
            when(it) {
                is ForecastViewData.Done -> viewModel.weatherUiState = ForecastViewData.Done(it.forecastDomainObject)
                is ForecastViewData.Loading -> viewModel.weatherUiState = ForecastViewData.Loading
                is ForecastViewData.Error -> viewModel.weatherUiState = ForecastViewData.Error(it.code, it.message)

            }
        }

    }

    when (viewModel.weatherUiState) {
        is ForecastViewData.Loading -> LoadingScreen(modifier)
        is ForecastViewData.Done -> ForecastList(
            (viewModel.weatherUiState as ForecastViewData.Done).forecastDomainObject.days, //TODO need to pass a list of days here
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
    onClick: () -> Unit,
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = 4.dp,
        onClick = onClick
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
            Spacer(modifier = Modifier.weight(1f))

            Column {
                Text(
                    text = "${day.day.maxtemp_f}\u00B0",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

            }
            Spacer(modifier = Modifier.weight(1f))
            WeatherConditionIcon(iconUrl = day.day.condition.icon)
        }

    }
}


