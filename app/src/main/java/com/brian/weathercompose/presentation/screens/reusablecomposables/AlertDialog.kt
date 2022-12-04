package com.brian.weathercompose.presentation.screens.reusablecomposables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.brian.weathercompose.BuildConfig

@Composable
fun CustomAlertDialog(
    tag: String,
    title: String,
    text: String,
    onDismissRequest: () -> Unit,
    dismissButton: @Composable () -> Unit = {},
    confirmButtonOnClick: () -> Unit,
    confirmText: String
) {
    AlertDialog(
        // This test tag is used for semantics matching in UI testing
        modifier = Modifier.semantics { testTag = tag },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = { Text(text = text) },
        onDismissRequest = onDismissRequest,
        dismissButton = dismissButton,
        confirmButton = {
            TextButton(onClick =
                confirmButtonOnClick
            ) {
                Text(text = confirmText)
            }
        }
    )
}