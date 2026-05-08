package com.umang.biotrace.presentation.resultscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umang.biotrace.presentation.capture.CaptureViewModel

@Composable
fun ResultScreen(
    viewModel: CaptureViewModel,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val metrics = state.result.lastMetrics

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Capture Result",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Blur Score: ${metrics?.blurScore?.format() ?: "-"}")
            Text(text = "Brightness Score: ${metrics?.brightnessScore?.format() ?: "-"}")
            Text(text = "Focus Distance: ${metrics?.focusDistance?.format() ?: "-"}")
            Text(text = "Camera: ${metrics?.cameraType ?: "-"}")
            Text(text = "Saved Fingers: ${state.result.fingerPaths.size}/5")

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onFinish) {
                Text(text = "Finish")
            }
        }
    }
}

private fun Float.format(): String = String.format("%.2f", this)
