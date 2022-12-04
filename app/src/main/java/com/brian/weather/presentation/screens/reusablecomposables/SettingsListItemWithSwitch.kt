package com.brian.weather.presentation.screens.reusablecomposables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brian.weather.presentation.SettingsDrawerItem
import com.brian.weather.presentation.theme.WeatherComposeTheme
import com.brian.weather.R

@Composable
fun SettingsListItemWithSwitch(
    item: SettingsDrawerItem,
    isChecked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // icon and unread bubble
        Icon(
            modifier = Modifier
                .size(size = 48.dp)
                .padding(start = 20.dp),
            painter = item.image,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.size(24.dp))

        // label
        Text(
            // modifier = Modifier.padding(start = 16.dp),
            text = item.label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChanged,
            enabled = enabled,
            modifier = Modifier.padding(end = 8.dp).semantics { testTag = item.label })


    }
}

@Preview(showSystemUi = true)
@Composable
fun CheckboxPreview() {
    WeatherComposeTheme() {
        SettingsListItemWithSwitch(
            item = SettingsDrawerItem(
                image = painterResource(R.drawable.ic_baseline_color_lens_24),
                label = "Dynamic Condition Colors",
            ),
            isChecked = true,
            onCheckedChanged = {}
        )

    }
}