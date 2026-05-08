package com.umang.biotrace.data

import android.content.Context
import com.umang.biotrace.domain.model.CameraMetrics
import org.json.JSONArray
import org.json.JSONObject

class CameraMetricStore(context: Context) {

    private val preferences = context.getSharedPreferences("camera_metric_store", Context.MODE_PRIVATE)

    fun save(metrics: CameraMetrics) {
        val allMetrics = JSONArray(preferences.getString(KEY_METRICS, "[]"))
        allMetrics.put(
            JSONObject()
                .put("deviceId", metrics.deviceId)
                .put("brightnessScore", metrics.brightnessScore.toDouble())
                .put("lightType", metrics.lightType.label)
                .put("cameraType", metrics.cameraType)
                .put("focalLength", metrics.focalLength.toDouble())
                .put("aperture", metrics.aperture.toDouble())
                .put("focusDistance", metrics.focusDistance.toDouble())
                .put("blurScore", metrics.blurScore.toDouble())
        )
        preferences.edit().putString(KEY_METRICS, allMetrics.toString()).apply()
    }

    companion object {
        private const val KEY_METRICS = "metrics"
    }
}
