package com.ateeb.kaioflow.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ateeb.kaioflow.data.repository.FocusRepository
import com.ateeb.kaioflow.data.repository.FocusRepositoryImpl
import com.ateeb.kaioflow.permissions.PermissionManager
import com.ateeb.kaioflow.permissions.UsageStatsPermissionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val permissionManager: PermissionManager = UsageStatsPermissionManager(),
    private val focusRepository: FocusRepository = FocusRepositoryImpl()
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun processIntent(intent: HomeIntent, context: Context) {
        when (intent) {
            is HomeIntent.SetFocusDuration -> {
                val timeInMillis =
                    ((intent.hours.toInt() * 3600) + (intent.minutes.toInt() * 60)) * 1000L
                _state.value = _state.value.copy(
                    selectedTimeInMillis = timeInMillis,
                    selectedHours = intent.hours,
                    selectedMinutes = intent.minutes
                )
            }

            is HomeIntent.UpdateHours -> {
                _state.value = _state.value.copy(selectedHours = intent.hours)
            }

            is HomeIntent.UpdateMinutes -> {
                _state.value = _state.value.copy(selectedMinutes = intent.minutes)
            }

            is HomeIntent.CheckPermissions -> {
                checkAndHandlePermissions(context)
            }

            is HomeIntent.OnPermissionResult -> {
                handlePermissionResult(context)
            }

            is HomeIntent.LoadInstalledApps -> {
                loadInstalledApps(context)
            }
        }
    }

    private fun checkAndHandlePermissions(context: Context) {
        val hasPermission = permissionManager.hasUsageStatsPermission(context)
        _state.value = _state.value.copy(hasUsagePermission = hasPermission)

        if (hasPermission) {
            loadInstalledApps(context)
        }
    }

    private fun handlePermissionResult(context: Context) {
        // Add delay to ensure system has processed the permission
        viewModelScope.launch {
            delay(1000)
            val hasPermission = permissionManager.hasUsageStatsPermission(context)
            _state.value = _state.value.copy(hasUsagePermission = hasPermission)

            if (hasPermission) {
                loadInstalledApps(context)
            }
        }
    }

    private fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val apps = focusRepository.getInstalledAppsWithUsage(context)
            _state.value = _state.value.copy(installedApps = apps)
        }
    }

}