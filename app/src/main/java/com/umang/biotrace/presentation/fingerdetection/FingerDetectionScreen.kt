package com.umang.biotrace.presentation.fingerdetection

import androidx.compose.foundation.layout.*
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.umang.biotrace.domain.model.FingerType
import com.umang.biotrace.presentation.capture.CaptureViewModel
import com.umang.biotrace.presentation.components.CameraPreview
import com.umang.biotrace.presentation.components.FingerOverlay
import java.util.concurrent.Executor

@Composable
fun FingerDetectionScreen(
    viewModel: CaptureViewModel,
    onCompleted: () -> Unit
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
            cameraFacing = state.cameraFacing,
            frameAnalysis = state.frameAnalysis,
            onFrameAnalyzed = viewModel::onFrameAnalysis,
            onCameraReady = { capture, executor ->
                imageCapture = capture
                captureExecutor = executor
            }
        )

        FingerOverlay(modifier = Modifier.fillMaxSize(), brightnessScore = state.frameAnalysis.brightnessScore)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Camera: ${state.cameraFacing.label}",
                    color = Color.White
                )
                TextButton(
                    enabled = !state.isCapturing,
                    onClick = viewModel::switchCamera
                ) {
                    Text(text = "Switch")
                }
            }
            Text(
                text = "Finger ${state.activeFingerIndex + 1}/${FingerType.entries.size}: ${state.currentFinger?.label.orEmpty()}",
                color = Color.White
            )
            Text(
                text = "Palm: ${state.capturedPalmHandSide?.label ?: "-"} | Current: ${state.detectedHandSide.label}",
                color = Color.White.copy(alpha = 0.86f)
            )
            Text(
                text = "Light: ${state.frameAnalysis.lightType.label} | Blur ${state.frameAnalysis.blurScore.format()}",
                color = Color.White.copy(alpha = 0.86f)
            )
            Text(
                text = "${state.frameAnalysis.aiProvider} | Detected fingers: ${state.frameAnalysis.fingerCount}",
                color = Color.White.copy(alpha = 0.86f)
            )
            Text(
                text = "Detected finger: ${state.frameAnalysis.detectedFinger?.label ?: "-"}",
                color = Color.White.copy(alpha = 0.86f)
            )
            if (state.frameAnalysis.dorsalDetected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Finger dorsal side detected, please show palm side finger which contains finger record or minutiae points",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = imageCapture != null && captureExecutor != null && !state.isCapturing,
                onClick = {
                    viewModel.captureFinger(
                        imageCapture = imageCapture ?: return@Button,
                        executor = captureExecutor ?: return@Button,
                        onCompleted = onCompleted
                    )
                }
            ) {
                Text(text = if (state.isCapturing) "Capturing..." else "Capture Finger")
            }
        }
    }
}

private fun Float.format(): String = String.format("%.2f", this)
