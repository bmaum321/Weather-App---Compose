package com.brian.weathercompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brian.weathercompose.R

@Composable
fun AddWeatherScreen(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    navAction: () -> Unit
) {
    var location by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(text = stringResource(id = R.string.search_for_places)) },
            singleLine = true
        )
        Spacer(modifier = modifier.size(120.dp))

        Button(onClick = { addWeather(navAction) }) {
            Text(stringResource(R.string.save))
        }
    }

}

private fun addWeather(navAction: ()-> Unit){
    // Add weather to database
    // TODO


    //Navigate back to main screen, is this the best way to do this?
    // Google recommended not passing the nav controller to other composeables
    run(navAction)


}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddWeatherPreview() {
    AddWeatherScreen(value = "", onValueChange = {}) {

    }
}
