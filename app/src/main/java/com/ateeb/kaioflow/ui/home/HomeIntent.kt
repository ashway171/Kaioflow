package com.ateeb.kaioflow.ui.home

sealed class HomeIntent {
    data object CheckPermissions : HomeIntent()
    data object OnPermissionResult : HomeIntent()
    data class SetFocusDuration(val hours: Float, val minutes: Float) : HomeIntent()
    data object LoadInstalledApps : HomeIntent()
    data class UpdateHours(val hours: Float) : HomeIntent()
    data class UpdateMinutes(val minutes: Float) : HomeIntent()
}
