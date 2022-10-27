package com.brian.weathercompose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brian.weathercompose.R
import com.brian.weathercompose.domain.WeatherDomainObject
import com.brian.weathercompose.ui.theme.WeatherComposeTheme
import com.brian.weathercompose.ui.viewmodels.WeatherListState

@Composable
fun MainWeatherListScreen(
    weatherUiState: WeatherListState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    when (weatherUiState) {
        is WeatherListState.Loading -> LoadingScreen(modifier)
        is WeatherListState.Success -> WeatherListScreen(
            weatherUiState.weatherDomainObjects,
            modifier,
            onClick
        )
        is WeatherListState.Error -> ErrorScreen(retryAction, modifier)
    }
}

/**
 * The home screen displaying the loading message.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.loading)
        )
    }
}

/**
 * The home screen displaying error message with re-attempt button.
 */
@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.loading_failed))
        Button(onClick = { }) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * The home screen displaying list of weather objects
 */
@Composable
fun WeatherListScreen(
    weatherDomainObjectList: List<WeatherDomainObject>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(weatherDomainObjectList) {
            WeatherListItem(it, onClick = onClick)
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
        onClick =  onClick,
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
            Column() {
                Text(text = weatherDomainObject.location)
                Text(text = weatherDomainObject.country)
                Text(text = weatherDomainObject.conditionText)
            }
            Spacer(modifier = Modifier.size(64.dp))

            Column() {
                Text(
                    text = "${weatherDomainObject.temp}\u00B0",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.size(64.dp))
            WeatherConditionIcon(weatherDomainObject = weatherDomainObject)
        }

    }
}

@Composable
fun WeatherConditionIcon(
    weatherDomainObject: WeatherDomainObject,
    modifier: Modifier = Modifier
) {

    AsyncImage(
        modifier = modifier
            .size(64.dp),
        model = ImageRequest.Builder(context = LocalContext.current)
            .data("https:" + weatherDomainObject.imgSrcUrl)
            .crossfade(true)
            .build(),
        error = painterResource(R.drawable.ic_broken_image),
        placeholder = painterResource(R.drawable.loading_img),
        contentDescription = weatherDomainObject.conditionText,
        contentScale = ContentScale.FillBounds,
    )
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
        WeatherListScreen(mockData, onClick = {})
    }
}
