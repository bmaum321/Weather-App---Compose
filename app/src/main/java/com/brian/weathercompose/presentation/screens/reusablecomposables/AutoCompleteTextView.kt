package com.brian.weathercompose.presentation.screens.reusablecomposables

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@Composable
fun <T> AutoCompleteTextView(
    modifier: Modifier,
    query: String,
    queryLabel: String,
    onQueryChanged: (String) -> Unit,
    searchResults: List<T>,
    onDoneActionClick: () -> Unit,
    onClearClick: () -> Unit,
    onItemClick: (T) -> Unit,
    itemContent: @Composable (T) -> Unit
) {

    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    QuerySearch(
        query = query,
        label = queryLabel,
        onQueryChanged = onQueryChanged,
        onDoneActionClick = {
            // view.clearFocus()
            onDoneActionClick()
        },
        onClearClick = {
            onClearClick()
        }
    )
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .heightIn(max = TextFieldDefaults.MinHeight * 6)
            .animateContentSize()
            .border(BorderStroke(2.dp, Color.Black))
    ) {
        if (searchResults.isNotEmpty()) {
            items(searchResults) { place ->
                Row(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            onItemClick(place)
                            view.clearFocus()
                            view.hideKeyboard()
                        }
                ) {
                    itemContent(place)
                }
                Divider(thickness = 2.dp)
            }
        }
    }
}

private fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
