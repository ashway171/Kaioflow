package com.ateeb.kaioflow.permissions

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

class UsageStatsPermissionManager : PermissionManager {
    override fun hasUsageStatsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkWithAppOpsManager(context)
        } else {
            checkWithUsageStatsQuery(context)
        }
    }

    override fun requestUsageStatsPermission(context: Context): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkWithAppOpsManager(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            checkWithUsageStatsQuery(context)
        }
    }

    private fun checkWithUsageStatsQuery(context: Context): Boolean {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 60_000 // Last minute

            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            usageStatsList?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }


}