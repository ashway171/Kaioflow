package com.ateeb.kaioflow.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.ateeb.kaioflow.ui.home.HomeScreen
import com.ateeb.kaioflow.ui.theme.KaioflowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaioflowTheme {
                HomeScreen(modifier = Modifier)
            }
        }
    }
}