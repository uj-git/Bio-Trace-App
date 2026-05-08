package com.umang.biotrace.presentation.palmdetection

import androidx.compose.foundation.layout.*
import androidx.camera.core.ImageCapture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.umang.biotrace.presentation.capture.CaptureViewModel
import com.umang.biotrace.presentation.components.CameraPreview
import com.umang.biotrace.presentation.components.PalmOverlay
import java.util.concurrent.Executor

@Composable
fun PalmDetectionScreen(
    viewModel: CaptureViewModel,
    onPalmCaptured: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var captureExecutor by remember { mutableStateOf<Executor?>(null) }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            frameAnalysis = state.frameAnalysis,
            onFrameAnalyzed = viewModel::onFrameAnalysis,
            onCameraReady = { capture, executor ->
                imageCapture = capture
                captureExecutor = executor
            }
        )

        PalmOverlay(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {

            Text(
                text = "Detected: ${state.detectedHandSide.label} | Light: ${state.frameAnalysis.lightType.label}",
                color = Color.White
            )
            Text(
                text = "Brightness ${state.frameAnalysis.brightnessScore.format()} | Blur ${state.frameAnalysis.blurScore.format()}",
                color = Color.White.copy(alpha = 0.86f)
            )

            if (state.frameAnalysis.dorsalDetected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Palm dorsal side detected, minutiae points won't be extracted.",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = imageCapture != null && captureExecutor != null && !state.isCapturing,
                onClick = {
                    viewModel.capturePalm(
                        imageCapture = imageCapture ?: return@Button,
                        executor = captureExecutor ?: return@Button,
                        onCaptured = onPalmCaptured
                    )
                }
            ) {
                Text(text = if (state.isCapturing) "Capturing..." else "Capture Palm")
            }
        }
    }
}

private fun Float.format(): String = String.format("%.2f", this)
