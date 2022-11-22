package com.brian.weathercompose.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
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
import androidx.preference.PreferenceManager
import com.brian.weathercompose.R
import com.brian.weathercompose.domain.model.AlertDomainObject
import com.brian.weathercompose.domain.model.ForecastDomainObject
import com.brian.weathercompose.presentation.screens.reusablecomposables.ErrorScreen
import com.brian.weathercompose.presentation.screens.reusablecomposables.LoadingScreen
import com.brian.weathercompose.presentation.viewmodels.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel


@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    location: String
) {
    val dailyForecastViewModel = getViewModel<DailyForecastViewModel>()
    // update title bar

    // This only seems to work if I pass the viewmodel all the way down from main activity and only have one instance of main view model, grabbing it from Koin doesnt work
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            mainViewModel.updateActionBarTitle("Weather Alerts")
        }
    }
    val context = LocalContext.current
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val state by remember {dailyForecastViewModel.getForecastForZipcode(location, pref, context.resources) }.collectAsState()

    when (state) {
        is ForecastViewData.Loading -> LoadingScreen(modifier)
        is ForecastViewData.Done -> AlertsList(
            (state as ForecastViewData.Done).forecastDomainObject,
            modifier,
            dailyForecastViewModel
        )
        is ForecastViewData.Error -> ErrorScreen({ dailyForecastViewModel.refresh() }, modifier)
    }
}



/**
 * Screen displaying Weather Alerts
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlertsList(
    forecast: ForecastDomainObject,
    modifier: Modifier = Modifier,
    viewModel: DailyForecastViewModel,

) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        viewModel.refresh()
        refreshing = false
    }

    val refreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refresh() }
    )
    val listState = rememberLazyListState()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val coroutineScope = rememberCoroutineScope()

        Box(modifier = Modifier.pullRefresh(refreshState)) {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(4.dp),
                state = listState
            ) {
                items(forecast.alerts) {
                    AlertListItem(it)
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = refreshState,
                Modifier.align(Alignment.TopCenter)
            )

            AnimatedVisibility(
                visible = showButton,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingActionButton(
                    onClick = {   coroutineScope.launch {
                        listState.animateScrollToItem(0, 0)
                    } },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_expand_less_24),
                        contentDescription = stringResource(R.string.scroll_to_top)
                    )
                }
            }
        }

}

@Composable
fun AlertListItem(
    alert: AlertDomainObject,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = 4.dp,

    ) {
        Column() {
            Text(
                text = alert.headline,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = alert.desc, modifier = Modifier.padding(8.dp))
        }
      
    }
}



