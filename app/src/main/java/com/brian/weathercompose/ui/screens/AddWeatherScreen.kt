package com.brian.weathercompose.ui.screens

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.W
import androidx.compose.ui.node.modifierElementOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brian.weathercompose.R
import com.brian.weathercompose.data.BaseApplication
import com.brian.weathercompose.ui.viewmodels.AddWeatherLocationViewModel
import com.brian.weathercompose.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddWeatherScreen(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    navAction: () -> Unit
) {
    val application = BaseApplication()
    val viewModel: AddWeatherLocationViewModel =
        viewModel(
            factory = AddWeatherLocationViewModel
                .AddWeatherLocationViewModelFactory(
                    application.database.weatherDao(),
                    application
                )
        )
    var location by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(text = stringResource(id = R.string.search_for_places)) },
            singleLine = true
        )
        Spacer(modifier = modifier.size(120.dp))


        Button(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                addWeather(navAction, viewModel, location, context)
            }
        }) {
            Text(stringResource(R.string.save))
        }
    }

}


private suspend fun addWeather(
    navAction: () -> Unit,
    viewModel: AddWeatherLocationViewModel,
    location: String,
    context: Context
) {
    // Add weather to database
    // TODO
    if (location.isNotBlank()) {
        if (!viewModel.storeNetworkDataInDatabase(location)) {
            withContext(Dispatchers.Main) {
                showToast(Constants.ERRORTEXT, context)
            }
        }

        //Navigate back to main screen
        withContext(Dispatchers.Main) {
            run(navAction)
        }

    }

}

private fun showToast(text: String?, context: Context) {
    val duration = Toast.LENGTH_LONG
    val toast = Toast.makeText(context, text, duration)
    toast.show()
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddWeatherPreview() {
    AddWeatherScreen(value = "", onValueChange = {}) {

    }
}
