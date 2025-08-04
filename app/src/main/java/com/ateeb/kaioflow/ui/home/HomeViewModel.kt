package com.ateeb.kaioflow.ui.home

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ateeb.kaioflow.permissions.PermissionManager
import com.ateeb.kaioflow.permissions.UsageStatsPermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HomeViewModel(private val permissionManager: PermissionManager = UsageStatsPermissionManager()) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun processIntent(intent: HomeIntent, context: Context) {
        when (intent) {
            is HomeIntent.SetFocusDuration -> {
                val timeInMillis =
                    ((intent.hours.toInt() * 3600) + (intent.minutes.toInt() * 60)) * 1000L
                _state.value = _state.value.copy(selectedTimeInMillis = timeInMillis,
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
                viewModelScope.launch {
                    val apps = getInstalledAppsWithUsage(context)
                    _state.value = _state.value.copy(installedApps = apps)
                }
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
            val apps = getInstalledAppsWithUsage(context)
            _state.value = _state.value.copy(installedApps = apps)
        }
    }

    private suspend fun getInstalledAppsWithUsage(context: Context, limit: Int=50): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.apply{
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        // Fetch usage stats once for all apps
        val usageStats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                currentTime
            )?.associateBy { it.packageName } ?: emptyMap()
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching Usage Stats: ${e.message}")
            emptyMap()
        }
        try {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                .filter { packageInfo ->
                    // Include only launchable apps
                    pm.getLaunchIntentForPackage(packageInfo.packageName) != null
                }
                .take(limit) // Limit the number of apps processed
                .map { packageInfo ->
                    val appName = packageInfo.applicationInfo?.loadLabel(pm).toString()  ?: packageInfo.packageName
                    val usageMinutes = usageStats[packageInfo.packageName]?.totalTimeInForeground?.div(1000 * 60) ?: 0L
                    Log.d("HomeViewModel", "${packageInfo.packageName} usage: $usageMinutes min")
                    AppInfo(
                        packageName = packageInfo.packageName,
                        name = appName,
                        usageMinutes = usageMinutes
                    )
                }
                .sortedByDescending { it.usageMinutes }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching third-party apps: ${e.message}")
            emptyList()
        }
    }

}