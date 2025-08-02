package com.ateeb.kaioflow.ui.common

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ateeb.kaioflow.permissions.PermissionManager
import com.ateeb.kaioflow.permissions.UsageStatsPermissionManager

@Composable
fun rememberUsageStatsPermission(
    permissionManager: PermissionManager = UsageStatsPermissionManager()
): UsageStatsPermissionState {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(permissionManager.hasUsageStatsPermission(context))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Add delay to ensure system processes the permission
        Handler(Looper.getMainLooper()).postDelayed({
            hasPermission = permissionManager.hasUsageStatsPermission(context)
        }, 1000)
    }

    return UsageStatsPermissionState(
        hasPermission = hasPermission,
        requestPermission = {
            launcher.launch(permissionManager.requestUsageStatsPermission(context))
        },
        refreshPermission = {
            hasPermission = permissionManager.hasUsageStatsPermission(context)
        }
    )
}

data class UsageStatsPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit,
    val refreshPermission: () -> Unit
)
