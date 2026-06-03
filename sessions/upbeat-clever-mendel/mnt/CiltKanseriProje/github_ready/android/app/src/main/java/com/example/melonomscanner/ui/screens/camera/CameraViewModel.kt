package com.example.melonomscanner.ui.screens.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melonomscanner.MelonomScannerApp
import com.example.melonomscanner.ml.QualityChecker
import com.example.melonomscanner.ml.RiskClassifier
import com.example.melonomscanner.ui.screens.metadata.MetadataViewModel
import com.example.melonomscanner.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class CameraUiState(
    val isProcessing: Boolean = false,
    val hintMessage: String? = null,
    val error: String? = null,
    val savedScanId: Long? = null
)

class CameraViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val app = MelonomScannerApp.from(application)
    private val _state = MutableStateFlow(CameraUiState())
    val state = _state.asStateFlow()

    private var imageCapture: ImageCapture? = null

    fun bindImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun capture(context: Context) {
        val capture = imageCapture ?: run {
            _state.update { it.copy(error = "Kamera hazır değil") }
            return
        }
        val metadata = MetadataViewModel.currentMetadata ?: run {
            _state.update { it.copy(error = "Önce hasta bilgilerini girin") }
            return
        }

        _state.update { it.copy(isProcessing = true, error = null, hintMessage = null) }

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    viewModelScope.launch {
                        try {
                            processCapture(context, image, metadata)
                        } finally {
                            image.close()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = "Fotoğraf alınamadı: ${exception.message}"
                        )
                    }
                }
            }
        )
    }

    private suspend fun processCapture(
        context: Context,
        image: ImageProxy,
        metadata: com.example.melonomscanner.data.model.PatientMetadata
    ) {
        val bitmap = imageProxyToBitmap(image) ?: run {
            _state.update {
                it.copy(isProcessing = false, error = "Görüntü dönüştürülemedi")
            }
            return
        }

        withContext(Dispatchers.Default) {
            // 1) Kalite kontrolü
            val quality = QualityChecker.evaluate(bitmap)
            if (!quality.isAcceptable) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        hintMessage = quality.hints.firstOrNull()
                    )
                }
                return@withContext
            }

            // 2) YOLO çıkarımı
            val prediction = app.yoloRunner.infer(bitmap)
            if (prediction == null) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        hintMessage = "Lezyon tespit edilemedi. Daha yakından deneyin."
                    )
                }
                return@withContext
            }

            // 3) Risk sınıflandırması
            val outcome = RiskClassifier.classify(prediction, metadata)

            // 4) Görseli kaydet
            val scanFile = ImageUtils.newScanFile(context)
            ImageUtils.saveJpeg(bitmap, scanFile)

            // 5) DB'ye yaz
            val id = app.scanRepository.save(
                outcome = outcome,
                metadata = metadata,
                imagePath = scanFile.absolutePath,
                heatmapPath = null,
                flagged = outcome.riskLevel.ordinal >= 2
            )
            _state.update {
                it.copy(isProcessing = false, savedScanId = id)
            }
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val buffer = image.planes.firstOrNull()?.buffer ?: return null
        val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val rotation = image.imageInfo.rotationDegrees
        return if (rotation == 0) bmp else {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        }
    }
}
