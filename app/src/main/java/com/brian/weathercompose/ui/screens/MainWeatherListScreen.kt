package com.brian.weathercompose.ui.screens

import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimatedVectorDrawable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brian.weathercompose.R
import com.brian.weathercompose.domain.WeatherDomainObject
import com.brian.weathercompose.ui.screens.reusablecomposables.ErrorScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.LoadingScreen
import com.brian.weathercompose.ui.screens.reusablecomposables.WeatherConditionIcon
import com.brian.weathercompose.ui.theme.WeatherComposeTheme
import com.brian.weathercompose.ui.viewmodels.WeatherListState
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel
import kotlinx.coroutines.launch

@Composable
fun MainWeatherListScreen(
    weatherUiState: WeatherListState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    addWeatherFabAction: () -> Unit,
    weatherListViewModel: WeatherListViewModel, //Should I be passing around a viewmodel like this to subcomposables?
) {
    when (weatherUiState) {
        is WeatherListState.Empty -> WeatherListScreen(
            emptyList<WeatherDomainObject>(),
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
@OptIn(ExperimentalMaterialApi::class)
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

    val state = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refresh() }
    )
    Scaffold(
        floatingActionButton = {
            AddWeatherFab(
                onClick = addWeatherFabAction
            )
        }
    ) { innerPadding ->

        Box(modifier = Modifier.pullRefresh(state)) {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(weatherDomainObjectList) { item ->
                    WeatherListItem(item, onClick = onClick)
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
        modifier = Modifier.padding(8.dp),
        elevation = 4.dp,
        onClick = { onClick(location) } // only way I can see to pass location is to pass a nav controller to this composable and do the navigation here
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
fun PhotosGridScreenPreview() {
    WeatherComposeTheme {
        val mockData = List(10) {
            WeatherDomainObject(
                "Liverpool", "32", "13088", "", "Sunny", 12.0,
                "SSW", "", 1, 1000, 1, "USA", "32"
            )
        }
        //   WeatherListScreen(mockData, onClick = {}, navAction = {}, weatherListViewModel = )
    }
}
