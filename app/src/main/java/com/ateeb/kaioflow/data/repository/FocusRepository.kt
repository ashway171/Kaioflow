package com.ateeb.kaioflow.data.repository

import android.content.Context
import com.ateeb.kaioflow.ui.home.AppInfo

interface FocusRepository {
    suspend fun getInstalledAppsWithUsage(context: Context, limit: Int = 50) : List<AppInfo>
}