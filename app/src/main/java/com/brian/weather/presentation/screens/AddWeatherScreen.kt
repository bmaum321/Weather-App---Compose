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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weather.R
import com.brian.weather.presentation.animations.pressClickEffect
import com.brian.weather.presentation.reusablecomposables.AutoCompleteTextView
import com.brian.weather.presentation.viewmodels.AddWeatherLocationViewModel
import com.brian.weather.presentation.viewmodels.SearchViewData
import com.brian.weather.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddWeatherScreen(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    navAction: () -> Unit
) {
    var input = value
    if(value == "{location}") {
        input = ""
    }
    val addWeatherLocationViewModel = getViewModel<AddWeatherLocationViewModel>()
    var location by remember { mutableStateOf(input) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchResults by addWeatherLocationViewModel.getSearchResults.collectAsState()
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
                if(location.isBlank()){
                    emptyList()
                } else {
                    when (searchResults) {
                        is SearchViewData.Done -> (searchResults as SearchViewData.Done).searchResults
                        is SearchViewData.Loading -> emptyList()
                        is SearchViewData.Error -> emptyList()
                    }
               },
                onClearClick = {
                    itemClicked = false
                    location = ""
                    addWeatherLocationViewModel.clearQueryResults()
                },
                onDoneActionClick = { keyboardController?.hide() },
                onItemClick = {
                    itemClicked = true
                    location = it
                    addWeatherLocationViewModel.clearQueryResults()
                },
                onQueryChanged = { updatedSearch ->
                    if (updatedSearch.length >= 3) {
                        addWeatherLocationViewModel.setQuery(updatedSearch)
                    } else if (updatedSearch.isBlank()) {
                        itemClicked = false
                        addWeatherLocationViewModel.clearQueryResults()
                    }
                    location = updatedSearch
                },
                itemContent = {
                    Text(text = it, fontSize = 14.sp, modifier = Modifier.animateContentSize())
                }

            )


            Spacer(modifier = modifier.size(120.dp))

            if(value == "{location}") {
                Button(
                    onClick = {
                        if(itemClicked) {
                            coroutineScope.launch(Dispatchers.IO) {
                                addWeather(navAction, addWeatherLocationViewModel, location, context)
                            }
                        }
                    },
                    modifier
                ) {
                    Text(stringResource(R.string.save))
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            editWeather(navAction, addWeatherLocationViewModel, location, context)
                        }
                    },
                    modifier.pressClickEffect()
                ) {
                    Text(stringResource(R.string.save))
                }
            }

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

private suspend fun editWeather(
    navAction: () -> Unit,
    viewModel: AddWeatherLocationViewModel,
    location: String,
    context: Context
) {
    withContext(Dispatchers.IO) {
        val entity = viewModel.getWeatherByZipcode(location).first()
        if (location.isNotBlank()) {
            viewModel.updateWeather(
                name = location,
                sortOrder = 0,
                zipcode = location,
                id = entity.id
            )
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
