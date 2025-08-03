package com.ateeb.kaioflow.ui.home

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.ateeb.kaioflow.R
import com.ateeb.kaioflow.ui.common.rememberUsageStatsPermission

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val permissionState = rememberUsageStatsPermission()

    // Load apps on first composition and when permission changes
    LaunchedEffect(permissionState.hasPermission) {
        Log.d(
            "HomeScreen",
            "LaunchedEffect triggered, hasPermission: ${permissionState.hasPermission}"
        )
        viewModel.processIntent(HomeIntent.LoadInstalledApps, context)
    }

    // Initial permission check
    LaunchedEffect(Unit) {
        viewModel.processIntent(HomeIntent.CheckPermissions, context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
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
        FocusTimeSelector(
            hours = state.selectedHours,
            minutes = state.selectedMinutes,
            onHoursChange = { hours ->
                viewModel.processIntent(HomeIntent.UpdateHours(hours), context)
            },
            onMinutesChange = { minutes ->
                viewModel.processIntent(HomeIntent.UpdateMinutes(minutes), context)
            },
            selectedTimeInMillis = state.selectedTimeInMillis,
            onSetFocusDuration = {
                viewModel.processIntent(
                    HomeIntent.SetFocusDuration(state.selectedHours, state.selectedMinutes),
                    context
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppListRender(
            apps = state.installedApps,
            hasPermission = permissionState.hasPermission,
            onRequestPermission = permissionState.requestPermission,
            isLoading = state.isLoading,
            error = state.error
        )
    }
}

@Composable
fun FocusTimeSelector(
    hours: Float,
    minutes: Float,
    onHoursChange: (Float) -> Unit,
    onMinutesChange: (Float) -> Unit,
    selectedTimeInMillis: Long,
    onSetFocusDuration: () -> Unit
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
        onValueChange = onHoursChange,
        valueRange = 0f..24f,
        steps = 23,
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
        onValueChange = onMinutesChange,
        valueRange = 0f..59f,
        steps = 58,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )

    Button(
        onClick = {
            onSetFocusDuration
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = when {
                hours.toInt() == 0 && minutes.toInt() == 0 -> {
                    "Set Focus Duration"
                }

                hours.toInt() == 0 -> {
                    "Set Focus ${minutes.toInt()}m"
                }

                minutes.toInt() == 0 -> {
                    "Set Focus ${hours.toInt()}h"
                }

                else -> {
                    "Set Focus ${hours.toInt()}h ${minutes.toInt()}m"
                }
            }, style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AppListRender(
    apps: List<AppInfo>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Text(
        text = "Installed Apps (${apps.size})",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    when {
        !hasPermission -> {
            PermissionRequiredContent(onRequestPermission)
        }

        isLoading -> {
            CircularProgressIndicator()
        }

        error != null -> {
            Text(
                text = "Error: $error",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        apps.isEmpty() -> {
            Text(
                text = "No apps found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            ) {
                items(apps) { app ->
                    AppListItem(app)
                }
            }
        }

    }
}

@Composable
fun AppListItem(app: AppInfo) {
    val context = LocalContext.current
    val iconRes = try {
        "android.resource://${app.packageName}/${
            context.packageManager.getApplicationInfo(app.packageName, 0).icon
        }"
    } catch (e: PackageManager.NameNotFoundException) {
        R.drawable.ic_launcher_foreground
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        AsyncImage(
            model =  ImageRequest.Builder(context)
                .data(iconRes)
                .crossfade(true)
                .build(),
            contentDescription = "${app.name} icon",
            modifier = Modifier
                .size(48.dp)
                .padding(end = 12.dp),
            error = painterResource(R.drawable.ic_launcher_foreground),
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
            contentScale = ContentScale.Fit
        )

        // App name and usage time
        Column {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${app.usageMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PermissionRequiredContent(onRequestPermission: () -> Unit) {
    Column {
        Text(
            text = "Please grant usage access to view app usage times",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Grant Usage Access")
        }
    }
}