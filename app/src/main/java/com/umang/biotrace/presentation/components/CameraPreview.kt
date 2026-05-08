package com.umang.biotrace.presentation.components

import android.view.MotionEvent
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.umang.biotrace.camera.GoogleHandLandmarkerDetector
import com.umang.biotrace.camera.LuminosityAnalyzer
import com.umang.biotrace.domain.model.CameraFacing
import com.umang.biotrace.domain.model.FrameAnalysis
import com.umang.biotrace.domain.model.LightType
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraFacing: CameraFacing,
    frameAnalysis: FrameAnalysis,
    onFrameAnalyzed: (FrameAnalysis) -> Unit,
    onCameraReady: (ImageCapture, Executor) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember(cameraFacing) {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val captureExecutor = remember { ContextCompat.getMainExecutor(context) }
    val handDetector = remember { GoogleHandLandmarkerDetector.create(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                previewView = this
            }
        },
        update = { view ->
            view.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    camera?.startTapToFocus(
                        previewView = view,
                        x = event.x,
                        y = event.y
                    )
                }
                true
            }
        }
    )

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(previewView, cameraFacing) {
        val view = previewView ?: return@LaunchedEffect
        val provider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider = provider

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraFacing.toLensFacing())
            .build()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(analysisExecutor, LuminosityAnalyzer(handDetector, onFrameAnalyzed))
            }

        provider.unbindAll()
        camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
        onCameraReady(imageCapture, captureExecutor)
    }

    LaunchedEffect(frameAnalysis.lightType, camera) {
        val exposureState = camera?.cameraInfo?.exposureState ?: return@LaunchedEffect
        if (!exposureState.isExposureCompensationSupported) return@LaunchedEffect
        val range = exposureState.exposureCompensationRange
        val target = when (frameAnalysis.lightType) {
            LightType.Low -> range.upper / 2
            LightType.Normal -> 0
            LightType.Bright -> range.lower / 2
        }.coerceIn(range.lower, range.upper)
        camera?.cameraControl?.setExposureCompensationIndex(target)
    }

    LaunchedEffect(camera, previewView) {
        val view = previewView ?: return@LaunchedEffect
        camera?.startTapToFocus(view, view.width / 2f, view.height / 2f)
    }

    DisposableEffect(Unit) {
        onDispose {
            handDetector?.close()
            analysisExecutor.shutdown()
            cameraProvider?.unbindAll()   // ← fixes the leak
        }
    }
}

private fun CameraFacing.toLensFacing(): Int = when (this) {
    CameraFacing.Rear -> CameraSelector.LENS_FACING_BACK
    CameraFacing.Front -> CameraSelector.LENS_FACING_FRONT
}

private fun Camera.startTapToFocus(
    previewView: PreviewView,
    x: Float,
    y: Float
) {
    val meteringPoint = previewView.meteringPointFactory.createPoint(x, y)
    val action = FocusMeteringAction.Builder(
        meteringPoint,
        FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
    )
        .setAutoCancelDuration(3, TimeUnit.SECONDS)
        .build()
    cameraControl.startFocusAndMetering(action)
}
