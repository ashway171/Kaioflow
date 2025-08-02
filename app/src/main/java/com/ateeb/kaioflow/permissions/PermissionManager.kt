package com.ateeb.kaioflow.permissions

import android.content.Context
import android.content.Intent

interface PermissionManager {
    fun hasUsageStatsPermission(context: Context): Boolean
    fun requestUsageStatsPermission(context: Context): Intent
}