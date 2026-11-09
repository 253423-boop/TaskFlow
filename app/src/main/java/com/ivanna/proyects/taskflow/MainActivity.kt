package com.ivanna.proyects.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.ivanna.proyects.taskflow.core.navigation.TaskFlowNavHost
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.ivanna.proyects.taskflow.ui.theme.TransparentColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparentArgb = TransparentColor.toArgb()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = transparentArgb,
                darkScrim = transparentArgb
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = transparentArgb,
                darkScrim = transparentArgb
            )
        )
        setContent {
            TaskFlowTheme {
                TaskFlowNavHost()
            }
        }
    }
}
