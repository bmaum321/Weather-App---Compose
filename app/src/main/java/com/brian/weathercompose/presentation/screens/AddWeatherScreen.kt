package com.brian.weathercompose.presentation.screens

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
import com.brian.weathercompose.R
import com.brian.weathercompose.presentation.screens.reusablecomposables.AutoCompleteTextView
import com.brian.weathercompose.presentation.viewmodels.AddWeatherLocationViewModel
import com.brian.weathercompose.presentation.viewmodels.SearchViewData
import com.brian.weathercompose.util.Constants
import kotlinx.coroutines.Dispatchers
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

    val addWeatherLocationViewModel = getViewModel<AddWeatherLocationViewModel>()
    var location by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchResults by addWeatherLocationViewModel.getSearchResults.collectAsState()


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
                when (searchResults) {
                    is SearchViewData.Done -> (searchResults as SearchViewData.Done).searchResults
                    is SearchViewData.Loading -> emptyList()
                    is SearchViewData.Error -> emptyList()
                },
                onClearClick = {
                    location = ""
                    addWeatherLocationViewModel.clearQueryResults()
                },
                onDoneActionClick = { keyboardController?.hide() },
                onItemClick = {
                    location = it
                    addWeatherLocationViewModel.clearQueryResults()
                },
                onQueryChanged = { updatedSearch ->
                    if (updatedSearch.length >= 3) {
                        addWeatherLocationViewModel.setQuery(updatedSearch)
                    } else if (updatedSearch.isBlank()) {
                        addWeatherLocationViewModel.clearQueryResults()
                    }
                    location = updatedSearch
                },
                itemContent = { Text(text = it, fontSize = 14.sp, modifier = Modifier.animateContentSize()) }

            )


            Spacer(modifier = modifier.size(120.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        addWeather(navAction, addWeatherLocationViewModel, location, context)
                    }
                },
                modifier
            ) {
                Text(stringResource(R.string.save))
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
