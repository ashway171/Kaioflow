package com.ateeb.kaioflow.ui.home

import androidx.compose.ui.graphics.ImageBitmap

data class HomeState (
    val selectedTimeInMillis: Long = 0L, // Focus time in milliseconds
    val installedApps: List<AppInfo> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,

    val selectedHours: Float = 0f,
    val selectedMinutes: Float = 0f
)

data class AppInfo(
    val packageName: String = "",
    val name: String = "",
    val usageMinutes: Long
)
