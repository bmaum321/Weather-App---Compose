package com.brian.weathercompose.presentation.screens.reusablecomposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weathercompose.R
import com.brian.weathercompose.presentation.SettingsDrawerItem
import com.brian.weathercompose.presentation.theme.WeatherComposeTheme

@Composable
fun SettingsListItemWithCheckbox(
    item: SettingsDrawerItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // icon and unread bubble


            Icon(
                modifier = Modifier
                    .size(size = 40.dp)
                    .padding(start =12.dp),
                painter = item.image,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.size(12.dp))

            // label
            Text(
               // modifier = Modifier.padding(start = 16.dp),
                text = item.label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        Spacer(modifier = Modifier.weight(1f))

            Checkbox(checked = true, onCheckedChange = { })



    }
}

@Preview(showSystemUi = true)
@Composable
fun CheckboxPreview() {
    WeatherComposeTheme() {
        SettingsListItemWithCheckbox(item = SettingsDrawerItem(
            image = painterResource(R.drawable.ic_baseline_color_lens_24),
            label = "Dynamic Condition Colors"
        )
        )

    }
}