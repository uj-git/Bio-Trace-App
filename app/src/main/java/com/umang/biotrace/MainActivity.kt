package com.umang.biotrace

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.rememberNavController
import com.umang.biotrace.base.BaseActivity
import com.umang.biotrace.presentation.navigation.AppNavGraph
import com.umang.biotrace.ui.theme.BioTraceTheme

class MainActivity : BaseActivity() {

    private val permissionsGranted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestRuntimePermissions { granted ->
            permissionsGranted.value = granted
        }

        setContent {
            BioTraceTheme {
                BioTraceApp(permissionsGranted = permissionsGranted.value)
            }
        }
    }
}

@Composable
fun BioTraceApp(permissionsGranted: Boolean) {
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        permissionsGranted = permissionsGranted
    )
}
