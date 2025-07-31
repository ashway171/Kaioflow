package com.ateeb.kaioflow.ui.home

sealed class HomeIntent {
    data class SetFocusDuration(val hours: Float, val minutes: Float) : HomeIntent()
}