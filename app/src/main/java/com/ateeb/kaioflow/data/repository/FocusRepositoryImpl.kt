package com.ateeb.kaioflow.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.ateeb.kaioflow.ui.home.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class FocusRepositoryImpl : FocusRepository {

    override suspend fun getInstalledAppsWithUsage(context: Context, limit: Int): List<AppInfo> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.apply {
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
                Log.e("FocusRepository", "Error fetching Usage Stats: ${e.message}")
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
                        val appName = packageInfo.applicationInfo?.loadLabel(pm).toString()
                            ?: packageInfo.packageName
                        val usageMinutes =
                            usageStats[packageInfo.packageName]?.totalTimeInForeground?.div(1000 * 60)
                                ?: 0L
                        Log.d(
                            "HomeViewModel",
                            "${packageInfo.packageName} usage: $usageMinutes min"
                        )
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