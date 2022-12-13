package com.brian.weather.presentation.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brian.weather.R

@Composable
fun PulseAlert(
    durationMillis: Int = 1000,
    maxPulseSize: Float = 96f,
    minPulseSize: Float = 64f,
    pulseColor: Color = Color.Red,
    centreColor: Color = Color.Red,
    onClick: () -> Unit = {}
){
    val infiniteTransition = rememberInfiniteTransition()
    val size by infiniteTransition.animateFloat(
        initialValue = minPulseSize,
        targetValue = maxPulseSize,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Box(Modifier.padding().size(96.dp)) {
//Card for Pulse Effect
        FloatingActionButton(
            modifier = Modifier
                .size(size.dp)
                .align(Alignment.Center)
                .alpha(alpha),
            containerColor = pulseColor,
            onClick = { }
        ) {}
//Card For inner circle
        FloatingActionButton(modifier = Modifier
            .size(minPulseSize.dp)
            .align(Alignment.Center),
            containerColor = centreColor,
            onClick = onClick
        ){
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_crisis_alert_24),
                contentDescription = stringResource(R.string.alert_fab_description)
            )
        }
    }
}