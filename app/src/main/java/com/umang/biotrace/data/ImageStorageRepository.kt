package com.umang.biotrace.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.umang.biotrace.domain.model.FingerType
import com.umang.biotrace.domain.model.HandSide
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ImageStorageRepository(private val context: Context) {

    suspend fun savePalm(imageCapture: ImageCapture, handSide: HandSide, executor: Executor): File {
        val time = timestamp()
        val file = outputFile("${handSide.filePrefix()}_$time.${handSide.palmExtension()}")
        val captureFile = if (handSide == HandSide.Left) outputFile("${handSide.filePrefix()}_${time}_temp.jpg") else file
        
        capture(imageCapture, captureFile, executor)
        if (handSide == HandSide.Left) {
            convertJpegToPng(captureFile, file)
            captureFile.delete()
        }
        return file
    }

    suspend fun saveFinger(
        imageCapture: ImageCapture,
        handSide: HandSide,
        fingerType: FingerType,
        executor: Executor
    ): File {
        val file = outputFile("${handSide.filePrefix()}_${fingerType.filePart}_${timestamp()}.jpg")
        capture(imageCapture, file, executor)
        return file
    }

    private fun outputFile(name: String): File {
        val root = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            FOLDER_NAME
        )
        if (!root.exists()) root.mkdirs()
        return File(root, name)
    }

    private suspend fun capture(imageCapture: ImageCapture, file: File, executor: Executor) {
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        suspendCoroutine { continuation ->
            imageCapture.takePicture(
                options,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        continuation.resume(Unit)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }

    private fun timestamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    private fun HandSide.filePrefix(): String = when (this) {
        HandSide.Left -> "Left_Hand"
        HandSide.Right -> "Right_Hand"
    }

    private fun HandSide.palmExtension(): String = when (this) {
        HandSide.Left -> "png"
        HandSide.Right -> "jpg"
    }

    private fun convertJpegToPng(source: File, destination: File) {
        val bitmap = BitmapFactory.decodeFile(source.absolutePath) ?: return
        FileOutputStream(destination).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    companion object {
        const val FOLDER_NAME = "Finger Data"
    }
}
