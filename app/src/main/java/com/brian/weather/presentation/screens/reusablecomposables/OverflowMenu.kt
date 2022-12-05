package com.brian.weather.presentation.screens.reusablecomposables

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brian.weather.R

@Composable
fun OverflowMenu(content: @Composable () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.action_menu),
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        content()
    }
}

@Composable
fun DeleteDropDownMenuItem(onClick: () -> Unit) {
    //Drop down menu item with an icon on its left
    DropdownMenuItem(
        onClick = onClick,
        leadingIcon = {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.action_menu),
                modifier = Modifier.size(24.dp)
            )
        },
        text = { Text("Delete") }
    )
}

@Composable
fun EditDropDownMenuItem(onClick: () -> Unit) {
    //Drop down menu item with an icon on its left
    DropdownMenuItem(
        onClick = onClick,
        leadingIcon = {
            Icon(
                Icons.Filled.Edit,
                contentDescription = stringResource(R.string.action_menu),
                modifier = Modifier.size(24.dp)
            )
        },
        text = { Text("Edit") }
    )
}

/*
@Composable
fun BookmarksDropDownItem(onClick : () -> Unit) {
    //Drop down menu item with an icon on its left
    DropdownMenuItem(onClick = onClick) {
        Icon(painter = painterResource(R.drawable.ic_bookmark_filled),
            contentDescription = stringResource(R.string.bookmark),
            modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.bookmark))

  }
}
 */
