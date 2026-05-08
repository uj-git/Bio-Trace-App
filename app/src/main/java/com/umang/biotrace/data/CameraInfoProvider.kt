package com.umang.biotrace.data

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.provider.Settings
import com.umang.biotrace.domain.model.CameraFacing
import com.umang.biotrace.domain.model.CameraMetrics
import com.umang.biotrace.domain.model.FrameAnalysis

class CameraInfoProvider(private val context: Context) {

    @SuppressLint("HardwareIds")
    fun buildMetrics(analysis: FrameAnalysis, cameraFacing: CameraFacing): CameraMetrics {
        val lensFacing = when (cameraFacing) {
            CameraFacing.Rear -> CameraCharacteristics.LENS_FACING_BACK
            CameraFacing.Front -> CameraCharacteristics.LENS_FACING_FRONT
        }
        val characteristics = findCamera(lensFacing)
        val focalLength = characteristics
            ?.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            ?.firstOrNull() ?: 0f
        val aperture = characteristics
            ?.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            ?.firstOrNull() ?: 0f
        val minFocusDistance = characteristics
            ?.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f

        return CameraMetrics(
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: Build.MODEL,
            brightnessScore = analysis.brightnessScore,
            lightType = analysis.lightType,
            cameraType = cameraFacing.label.lowercase(),
            focalLength = focalLength,
            aperture = aperture,
            focusDistance = minFocusDistance,
            blurScore = analysis.blurScore
        )
    }

    private fun findCamera(lensFacing: Int): CameraCharacteristics? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return manager.cameraIdList
            .map { manager.getCameraCharacteristics(it) }
            .firstOrNull { it.get(CameraCharacteristics.LENS_FACING) == lensFacing }
    }
}
