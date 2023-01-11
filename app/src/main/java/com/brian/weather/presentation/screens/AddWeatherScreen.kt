package com.brian.weather.presentation.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weather.R
import com.brian.weather.presentation.reusablecomposables.AutoCompleteTextView
import com.brian.weather.presentation.viewmodels.AddWeatherLocationViewModel
import com.brian.weather.presentation.viewmodels.AddWeatherScreenEvent
import com.brian.weather.presentation.viewmodels.SearchState
import com.brian.weather.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddWeatherScreen(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    navAction: () -> Unit,
    searchResults: SearchState,
    onEvent: (AddWeatherScreenEvent) -> Boolean
) {
    var location by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var itemClicked by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.padding(32.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            AutoCompleteTextView(
                modifier = Modifier.fillMaxWidth(),
                query = location,
                queryLabel = context.getString(R.string.search_for_places),
                searchResults =
                // This will clear results if query is cleared with backspace key
                if (location.isBlank()) {
                    emptyList()
                } else {
                    when (searchResults) {
                        is SearchState.Success -> searchResults.searchResults
                        is SearchState.Loading -> emptyList()
                        is SearchState.Error -> emptyList()
                    }
                },
                onClearClick = {
                    itemClicked = false
                    location = ""
                    onEvent(AddWeatherScreenEvent.ClearQuery)
                },
                onDoneActionClick = { keyboardController?.hide() },
                onItemClick = {
                    itemClicked = true
                    location = it
                    onEvent(AddWeatherScreenEvent.ClearQuery)
                },
                onQueryChanged = { updatedSearch ->
                    if (updatedSearch.length >= 3) {
                        onEvent(AddWeatherScreenEvent.SetQuery(updatedSearch))
                    } else if (updatedSearch.isBlank()) {
                        itemClicked = false
                        onEvent(AddWeatherScreenEvent.ClearQuery)
                    }
                    location = updatedSearch
                },
                itemContent = {
                    Text(text = it, fontSize = 14.sp, modifier = Modifier.animateContentSize())
                }

            )


            Spacer(modifier = modifier.size(120.dp))

            Button(
                onClick = {
                    /**
                     * I need to add a delay here after using the callback method, to allow the insertion
                     * to complete before popping back stack
                     */
                    // if (itemClicked) {
                    coroutineScope.launch {
                        addWeather(navAction, location, context, onEvent)
                        delay(250)
                        run(navAction)
                    }
                    //    }
                },
                modifier
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}


private fun addWeather(
    navAction: () -> Unit,
    location: String,
    context: Context,
    onEvent: (AddWeatherScreenEvent) -> Boolean
) {
    // Add weather to database
    if (location.isNotBlank()) {
        if (onEvent(AddWeatherScreenEvent.SaveQueryInDatabase(location))) {
            showToast(Constants.ERRORTEXT, context)
        }
    }
}


private fun showToast(text: String?, context: Context) {
    val duration = Toast.LENGTH_LONG
    val toast = Toast.makeText(context, text, duration)
    toast.show()
}



