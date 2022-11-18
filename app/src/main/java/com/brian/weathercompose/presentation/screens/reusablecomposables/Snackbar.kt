package com.brian.weathercompose.presentation.screens.reusablecomposables

import androidx.compose.foundation.clickable
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SnackbarHost() {

}

@Composable
fun Snackbar(
    onClick: () -> Unit,
    message: String
) {
    Snackbar(
        action = {
            Text(
                text = "Undo",
                modifier = Modifier.clickable {
                    onClick
                }
            )
        }
    ) {
        Text(text = message)
    }
}
