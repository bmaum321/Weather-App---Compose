package com.brian.weather.presentation.reusablecomposables

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brian.weather.R

@Composable
fun WeatherConditionIcon(
    iconUrl: String,
    modifier: Modifier = Modifier,
    iconSize: Int
) {
    AsyncImage(
        modifier = modifier
            .size(iconSize.dp),
        model = ImageRequest.Builder(context = LocalContext.current)
            .data("https:$iconUrl")
            .crossfade(true)
            .build(),
        error = painterResource(R.drawable.ic_broken_image),
        placeholder = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.weather_condition_icon),
        contentScale = ContentScale.FillBounds,
    )
}
