package com.brian.weather.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weather.R
import com.brian.weather.domain.model.AlertDomainObject
import com.brian.weather.domain.model.ForecastDomainObject
import com.brian.weather.presentation.animations.pressClickEffect
import com.brian.weather.presentation.reusablecomposables.ErrorScreen
import com.brian.weather.presentation.reusablecomposables.LoadingScreen
import com.brian.weather.presentation.viewmodels.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel


@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    location: String
) {
    val dailyForecastViewModel = getViewModel<DailyForecastViewModel>()
    val state by remember {dailyForecastViewModel.getForecastForZipcode(location) }.collectAsState()

    when (state) {
        is ForecastState.Loading -> LoadingScreen(modifier)
        is ForecastState.Success -> AlertsList(
            (state as ForecastState.Success).forecastDomainObject,
            modifier,
            dailyForecastViewModel
        )
        is ForecastState.Error -> ErrorScreen({ dailyForecastViewModel.refresh() }, modifier)
    }
}



/**
 * Screen displaying Weather Alerts
 */
@OptIn(ExperimentalAnimationApi::class)
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

   // val refreshState = rememberPullRefreshState(
  //      refreshing = refreshing,
  //      onRefresh = { refresh() }
  //  )
    val listState = rememberLazyListState()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val coroutineScope = rememberCoroutineScope()

       // Box(modifier = Modifier.pullRefresh(refreshState)) {
    Box {
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
           // PullRefreshIndicator(
            //    refreshing = refreshing,
             //   state = refreshState,
            //    Modifier.align(Alignment.TopCenter)
          //  )

            AnimatedVisibility(
                visible = showButton,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {   coroutineScope.launch {
                        listState.animateScrollToItem(0, 0)
                    } },
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

@Composable
fun AlertListItem(
    alert: AlertDomainObject,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.padding(8.dp),
       // elevation = 4.dp,

    ) {
        Column {
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



