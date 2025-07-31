package com.ateeb.kaioflow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    var hours by remember { mutableFloatStateOf(0f) }
    var minutes by remember { mutableFloatStateOf(0f) }
    val state = viewModel.state.value

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pick your focus time, master",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "Hours: ${hours.toInt()}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = hours,
            onValueChange = { hours = it },
            valueRange = 0f..24f,
            steps = 23, // 0 to 24 inclusive
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Text(
            text = "Minutes: ${minutes.toInt()}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
        )
        Slider(
            value = minutes,
            onValueChange = { minutes = it },
            valueRange = 0f..59f,
            steps = 58, // 0 to 59 inclusive
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Button(
            onClick = {
                viewModel.processIntent(
                    HomeIntent.SetFocusDuration(hours = hours, minutes = minutes)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (state.selectedTimeInMillis > 0) {
                    val h = state.selectedTimeInMillis / 3600000
                    val m = (state.selectedTimeInMillis / 60000) % 60
                    if (h.toInt() == 0) {
                        "Focus ${m}m"
                    } else
                        "Focus $h h ${m}m"
                } else {
                    "Set Focus Duration"
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}