package com.ateeb.kaioflow.ui.home

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ateeb.kaioflow.R
import com.ateeb.kaioflow.permissions.PermissionManager
import com.ateeb.kaioflow.permissions.UsageStatsPermissionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
            Log.d("APPS_REQ", _state.value.toString())
        }
    }

    private fun getInstalledAppsWithUsage(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val defaultDrawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!
        val apps = try {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                .filter { packageInfo ->
                    // Include only launchable apps
                    pm.getLaunchIntentForPackage(packageInfo.packageName) != null
                }
                .map { packageInfo ->
                    val appName = packageInfo.applicationInfo?.loadLabel(pm).toString()  ?: packageInfo.packageName
                    val iconDrawable = try {
                        packageInfo.applicationInfo?.loadIcon(pm) ?: defaultDrawable
                    } catch (e: Exception) {
                        defaultDrawable
                    }
                    val icon = iconDrawable.toImageBitmap()
                    AppInfo(
                        packageName = packageInfo.packageName,
                        name = appName,
                        icon = icon,
                        usageMinutes = getDailyUsageMinutes(
                            context, packageInfo.packageName
                        )
                    )
                }
                .sortedByDescending { it.usageMinutes }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching third-party apps: ${e.message}")
            emptyList()
        }
        return apps
    }

    private fun getDailyUsageMinutes(context: Context, packageName: String): Long {
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
        val stats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                currentTime
            )
        } catch (e: Exception) {
            null
        }
        val usage = stats?.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
        Log.d("HomeViewModel", "$packageName usage: ${usage / (1000 * 60)} min")
        return usage / (1000 * 60)
    }

    private fun Drawable.toImageBitmap(): ImageBitmap {
        val bitmap = Bitmap.createBitmap(
            intrinsicWidth.coerceAtLeast(1),
            intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap.asImageBitmap()
    }

}