package com.brian.weathercompose.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weathercompose.R
import com.brian.weathercompose.presentation.screens.reusablecomposables.LoadingScreen
import com.brian.weathercompose.presentation.theme.WeatherComposeTheme
import kotlinx.coroutines.Job

@Composable
fun DrawerContent(
    gradientColors: List<Color> = listOf(Color(0xFFF70A74), Color(0xFFF59118)),
    itemClick: (String) -> Unit
) {

    val itemsList = prepareNavigationDrawerItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Box(modifier = Modifier){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
              ) {
                    Image(
                        modifier = Modifier
                            .size(size = 120.dp),
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = ""
                    )

                    Text(
                        text = "Weather Tracker",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                }
            }

        }

        items(itemsList) { item ->
            SettingsListItem(
                item = item,
                itemClick = { itemClick(item.label) }
            )
        }
    }
}

@Composable
fun SettingsListItem(
    item: SettingsDrawerItem,
    unreadBubbleColor: Color = Color(0xFF0FFF93),
    itemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                itemClick()
            }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // icon and unread bubble
        Box {

            Icon(
                modifier = Modifier
                    .padding(all = if (item.showUnreadBubble && item.label == "Messages") 5.dp else 2.dp)
                    .size(size = if (item.showUnreadBubble && item.label == "Messages") 24.dp else 28.dp),
                painter = item.image,
                contentDescription = null,
                tint = Color.White
            )

            // unread bubble
            if (item.showUnreadBubble) {
                Box(
                    modifier = Modifier
                        .size(size = 8.dp)
                        .align(alignment = Alignment.TopEnd)
                        .background(color = unreadBubbleColor, shape = CircleShape)
                )
            }
        }

        // label
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = item.label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun prepareNavigationDrawerItems(): List<SettingsDrawerItem> {
    val itemsList = mutableListOf<SettingsDrawerItem>()

    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(R.drawable.ic_temperature),
            label = "Units"
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_format_paint_24),
            label = "Interface",
            showUnreadBubble = false
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_notifications_24),
            label = "Notifications",
            showUnreadBubble = false
        )
    )
    itemsList.add(
        SettingsDrawerItem(
            image = painterResource(id = R.drawable.ic_baseline_help_24),
            label = "About"
        )
    )


    return itemsList
}

data class SettingsDrawerItem(
    val image: Painter,
    val label: String,
    val showUnreadBubble: Boolean = false
)

@Preview(showSystemUi = true)
@Composable
fun DrawerPreview() {
    WeatherComposeTheme {
        DrawerContent(itemClick = {})
    }
}