package com.brian.weathercompose.presentation.screens.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

@ExperimentalAnimationApi
fun tickerAnimation(duration: Int = 1000): ContentTransform {
    return slideInVertically(animationSpec = tween(durationMillis = duration)) { height -> height } + fadeIn(
        animationSpec = tween(durationMillis = duration)
    ) with slideOutVertically(animationSpec = tween(durationMillis = duration)) { height -> -height } + fadeOut(
        animationSpec = tween(durationMillis = duration)
    )
}