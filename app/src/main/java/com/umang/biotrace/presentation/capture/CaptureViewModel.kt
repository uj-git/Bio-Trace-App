package com.umang.biotrace.presentation.capture

import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umang.biotrace.data.CameraInfoProvider
import com.umang.biotrace.data.CameraMetricStore
import com.umang.biotrace.data.FingerValidation
import com.umang.biotrace.data.HandDetectionEngine
import com.umang.biotrace.data.ImageStorageRepository
import com.umang.biotrace.domain.model.CameraFacing
import com.umang.biotrace.domain.model.CaptureResult
import com.umang.biotrace.domain.model.FingerType
import com.umang.biotrace.domain.model.FrameAnalysis
import com.umang.biotrace.domain.model.HandSide
import com.umang.biotrace.domain.model.MinutiaeRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class CaptureViewModel(
    private val imageStorageRepository: ImageStorageRepository,
    private val cameraInfoProvider: CameraInfoProvider,
    private val metricStore: CameraMetricStore,
    private val handDetectionEngine: HandDetectionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private var palmRecords: List<MinutiaeRecord> = emptyList()

    fun onFrameAnalysis(analysis: FrameAnalysis) {
        _uiState.update { it.copy(frameAnalysis = analysis, detectedHandSide = analysis.estimatedHandSide) }
    }

    fun switchCamera() {
        _uiState.update {
            it.copy(
                cameraFacing = when (it.cameraFacing) {
                    CameraFacing.Rear -> CameraFacing.Front
                    CameraFacing.Front -> CameraFacing.Rear
                }
            )
        }
    }

    fun capturePalm(imageCapture: ImageCapture, executor: Executor, onCaptured: () -> Unit) {
        val state = uiState.value
        val analysis = state.frameAnalysis
        if (analysis.dorsalDetected) {
            showMessage("Palm dorsal side detected, minutiae points won't be extracted.")
            return
        }

        val handSide = analysis.estimatedHandSide
        _uiState.update { it.copy(isCapturing = true, detectedHandSide = handSide, statusMessage = null) }
        viewModelScope.launch {
            runCatching {
                val file = imageStorageRepository.savePalm(imageCapture, handSide, executor)
                val metrics = cameraInfoProvider.buildMetrics(analysis, state.cameraFacing)
                metricStore.save(metrics)
                palmRecords = handDetectionEngine.extractPalmRecords(handSide, analysis)
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        detectedHandSide = handSide,
                        capturedPalmHandSide = handSide,
                        activeFingerIndex = 0,
                        result = it.result.copy(palmPath = file.absolutePath, lastMetrics = metrics)
                    )
                }
            }.onSuccess {
                onCaptured()
            }.onFailure { error ->
                _uiState.update { it.copy(isCapturing = false) }
                showMessage(error.message ?: "Unable to capture palm")
            }
        }
    }

    fun captureFinger(imageCapture: ImageCapture, executor: Executor, onCompleted: () -> Unit) {
        val state = uiState.value
        val analysis = state.frameAnalysis
        val expectedFinger = state.currentFinger ?: return
        val capturedPalmHandSide = state.capturedPalmHandSide

        if (capturedPalmHandSide == null) {
            showMessage("Capture palm before scanning fingers")
            return
        }

        if (analysis.blurScore < 0.18f) {
            showMessage("Image is blurred. Please recapture the finger.")
            return
        }

        when (val validation = handDetectionEngine.validateFinger(capturedPalmHandSide, expectedFinger, palmRecords, analysis)) {
            FingerValidation.Dorsal -> {
                showMessage("Finger dorsal side detected, please show palm side finger which contains finger record or minutiae points")
                return
            }

            FingerValidation.IncorrectFinger -> {
                showMessage("Incorrect Finger")
                return
            }

            FingerValidation.NoMatch -> {
                showMessage("Finger does not match")
                return
            }

            is FingerValidation.Match -> {
                showMessage("${validation.fingerType.label} finger from ${capturedPalmHandSide.label}")
            }
        }

        _uiState.update { it.copy(isCapturing = true) }
        viewModelScope.launch {
            runCatching {
                val file = imageStorageRepository.saveFinger(imageCapture, capturedPalmHandSide, expectedFinger, executor)
                val metrics = cameraInfoProvider.buildMetrics(analysis, state.cameraFacing)
                metricStore.save(metrics)
                _uiState.update {
                    val nextIndex = it.activeFingerIndex + 1
                    it.copy(
                        isCapturing = false,
                        activeFingerIndex = nextIndex,
                        result = it.result.copy(
                            fingerPaths = it.result.fingerPaths + file.absolutePath,
                            lastMetrics = metrics
                        )
                    )
                }
            }.onSuccess {
                if (_uiState.value.activeFingerIndex >= FingerType.entries.size) onCompleted()
            }.onFailure { error ->
                _uiState.update { it.copy(isCapturing = false) }
                showMessage(error.message ?: "Unable to capture finger")
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }
}

data class CaptureUiState(
    val frameAnalysis: FrameAnalysis = FrameAnalysis(),
    val detectedHandSide: HandSide = HandSide.Left,
    val capturedPalmHandSide: HandSide? = null,
    val cameraFacing: CameraFacing = CameraFacing.Rear,
    val activeFingerIndex: Int = 0,
    val isCapturing: Boolean = false,
    val statusMessage: String? = null,
    val result: CaptureResult = CaptureResult()
) {
    val currentFinger: FingerType?
        get() = FingerType.entries.getOrNull(activeFingerIndex)
}
