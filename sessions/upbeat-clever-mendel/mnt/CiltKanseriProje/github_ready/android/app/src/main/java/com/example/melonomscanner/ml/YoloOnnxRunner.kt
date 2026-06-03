package com.example.melonomscanner.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.melonomscanner.data.model.BBox
import com.example.melonomscanner.data.model.LesionClass
import java.nio.FloatBuffer
import java.util.Collections
import kotlin.math.exp

/**
 * ONNX Runtime tabanlı cilt lezyonu sınıflandırma çıkarımı.
 *
 * Aktif Model: **Swin-Tiny FP16** (timm: swin_tiny_patch4_window7_224.ms_in22k_ft_in1k)
 *   ImageNet-22k pretrained → ISIC 7-class fine-tuned
 *   Boyut: ~55 MB (FP16), 27.5M parametre
 *
 * Test seti sonuçları (n = 3.684):
 *   accuracy 0.6444 | macro F1 0.6076 | **mel recall 0.8812** | kappa 0.5242
 *
 * Eski YOLO11m-cls (referans):
 *   accuracy 0.7705 | macro F1 0.6762 | mel recall 0.6743 | kappa 0.6664
 *
 * **NEDEN SWIN?** Mel recall ~0.88 ile YOLO'dan %30 daha yüksek mel-yakalama oranı sunar.
 * Mel-odaklı tarama senaryosunda en güçlü tek-model seçeneğidir.
 * Trade-off: APK boyutu 25 MB → 55 MB; tek-model accuracy YOLO'dan düşük; ancak
 * threshold tuning ile dengelenir.
 *
 * Mel-recall'u arttırmak için **client-side threshold tuning** uygulanır:
 *   if p_mel >= MEL_THRESHOLD → predict mel
 *   else → argmax(probs)
 *
 * Swin için önerilen threshold değerleri (yeniden kalibre):
 *   0.20 → tarama (mel recall ~0.92)
 *   0.30 → karar destek (mel recall ~0.88) ← varsayılan
 *   0.45 → yüksek-precision (mel recall ~0.82)
 *
 * Girdi: (1, 3, 384, 384), RGB.
 *   useImageNetNormalize = true  → (pixel/255 − mean) / std  (timm/Swin için)
 *   useImageNetNormalize = false → pixel/255                   (Ultralytics YOLO için)
 *
 * Çıktı: (1, 7) logits — softmax yazılım tarafında uygulanır.
 *
 * **ÖNEMLİ:** Sınıf sırası **alfabetik** (Ultralytics + timm uyumlu):
 *   [0]=akiec, [1]=bcc, [2]=bkl, [3]=df, [4]=mel, [5]=nv, [6]=vasc
 */
class YoloOnnxRunner(
    context: Context,
    modelAssetName: String = "best.onnx",
    private val inputSize: Int = 384,
    private val melThreshold: Float = 0.30f,
    private val useImageNetNormalize: Boolean = true
) : AutoCloseable {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val floatBuffer: FloatBuffer = FloatBuffer.allocate(1 * 3 * inputSize * inputSize)
    private val pixelBuffer: IntArray = IntArray(inputSize * inputSize)

    init {
        val bytes = context.assets.open(modelAssetName).use { it.readBytes() }
        val opts = OrtSession.SessionOptions().apply {
            setIntraOpNumThreads(Runtime.getRuntime().availableProcessors().coerceAtMost(4))
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        }
        session = env.createSession(bytes, opts)
        Log.i(
            TAG,
            "YOLO11-cls ONNX yüklendi (${bytes.size / 1024} KB). " +
                "inputs=${session.inputNames}, outputs=${session.outputNames}, " +
                "inputSize=$inputSize, melThreshold=$melThreshold"
        )
    }

    /**
     * @return [YoloPrediction] — sınıflandırma çıktısı; boundingBox her zaman null.
     *         Hata durumunda null döner (CameraViewModel hint mesajı gösterir).
     */
    fun infer(bitmap: Bitmap): YoloPrediction? {
        return try {
            inferInternal(bitmap)
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "OOM inference sırasında", oom)
            null
        } catch (t: Throwable) {
            Log.e(TAG, "Inference exception (${t.javaClass.simpleName}): ${t.message}", t)
            null
        }
    }

    private fun inferInternal(bitmap: Bitmap): YoloPrediction? {
        // 1) Preprocessing: resize-shortside + center-crop → 384x384
        val processed = resizeAndCenterCrop(bitmap, inputSize)
        if (processed.width != inputSize || processed.height != inputSize) {
            Log.e(TAG, "Beklenmeyen bitmap boyutu: ${processed.width}x${processed.height}")
            return null
        }

        // 2) NCHW float32 tensor oluştur
        val inputName = session.inputNames.iterator().next()
        val tensor = OnnxTensor.createTensor(
            env,
            bitmapToNchw(processed),
            longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())
        )

        val startTime = System.currentTimeMillis()
        val result = try {
            session.run(Collections.singletonMap(inputName, tensor))
        } finally {
            tensor.close()
        }
        val latency = System.currentTimeMillis() - startTime

        // 3) Output parse — Ultralytics farklı versiyonlarda farklı şekiller döndürebilir
        val probs = try {
            extractProbs(result.get(0).value)
        } finally {
            result.close()
        }

        if (probs == null) {
            Log.w(TAG, "Output parse başarısız")
            return null
        }
        if (probs.size < ULTRA_ORDER.size) {
            Log.e(TAG, "Output uzunluğu yetersiz: ${probs.size} < ${ULTRA_ORDER.size}")
            return null
        }

        // 4) Eğer model softmax uygulanmamış (logits) gelirse, software-side softmax uygula
        val needsSoftmax = probs.sum() < 0.99f || probs.sum() > 1.01f || probs.any { it < 0f }
        val finalProbs = if (needsSoftmax) softmax(probs.sliceArray(0 until ULTRA_ORDER.size)) else
            probs.sliceArray(0 until ULTRA_ORDER.size)

        return parseClsOutput(finalProbs, latency)
    }

    /**
     * Ultralytics ONNX cls çıktısı versiyona göre 1D/2D/3D olabilir.
     * Mümkün olan tüm şekilleri destekle:
     *   FloatArray(7)              → düz olasılıklar
     *   Array<FloatArray>(1, 7)    → batch boyutlu (yaygın)
     *   Array<Array<FloatArray>>   → bazı eski export'lar
     */
    @Suppress("UNCHECKED_CAST")
    private fun extractProbs(value: Any?): FloatArray? {
        return try {
            when (value) {
                is FloatArray -> value
                is Array<*> -> {
                    if (value.isEmpty()) return null
                    when (val inner = value[0]) {
                        is FloatArray -> inner   // (1, 7) → ilk batch elemanını al
                        is Array<*> -> {          // (1, 7, ?) → flatten
                            val firstInner = inner[0]
                            if (firstInner is FloatArray) firstInner
                            else null
                        }
                        else -> null
                    }
                }
                else -> {
                    Log.e(TAG, "Tanımsız output tipi: ${value?.javaClass?.name}")
                    null
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "extractProbs hatası: ${t.message}", t)
            null
        }
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.max()
        val exps = FloatArray(logits.size) { exp((logits[it] - maxLogit).toDouble()).toFloat() }
        val sum = exps.sum().coerceAtLeast(1e-9f)
        return FloatArray(logits.size) { exps[it] / sum }
    }

    /**
     * Output: [7] — softmax olasılıkları (Ultralytics alfabetik sırası).
     * Mel-odaklı threshold uygulanır.
     */
    private fun parseClsOutput(probs: FloatArray, latencyMs: Long): YoloPrediction {
        // Tüm sınıflar için olasılık listesi (LesionClass enum'a maplenmiş, sıralı)
        val perClass: List<Pair<LesionClass, Float>> = ULTRA_ORDER
            .mapIndexed { i, cls -> cls to probs[i] }
            .sortedByDescending { it.second }

        // Argmax
        var argmaxIdx = 0
        var argmaxProb = probs[0]
        for (i in 1 until probs.size.coerceAtMost(ULTRA_ORDER.size)) {
            if (probs[i] > argmaxProb) { argmaxIdx = i; argmaxProb = probs[i] }
        }

        // Threshold-tuning kuralı
        val melProb = probs[ULTRA_MEL_IDX]
        val finalClass: LesionClass
        val finalConfidence: Float
        if (melProb >= melThreshold) {
            finalClass = LesionClass.MEL
            finalConfidence = melProb
        } else {
            finalClass = ULTRA_ORDER[argmaxIdx]
            finalConfidence = argmaxProb
        }

        Log.d(
            TAG,
            "Inference OK: argmax=${ULTRA_ORDER[argmaxIdx].code} (${"%.3f".format(argmaxProb)}), " +
                "mel=${"%.3f".format(melProb)}, final=${finalClass.code}, latency=${latencyMs}ms"
        )

        return YoloPrediction(
            primaryClass = finalClass,
            confidence = finalConfidence,
            boundingBox = null,
            topKProbabilities = perClass,
            inferenceTimeMs = latencyMs
        )
    }

    /**
     * Ultralytics cls preprocessing eşdeğeri:
     *   1) Kısa kenarı [target] olacak şekilde resize (en-boy oranını koru)
     *   2) Merkez crop [target] x [target]
     */
    private fun resizeAndCenterCrop(bitmap: Bitmap, target: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        require(w > 0 && h > 0) { "Geçersiz bitmap boyutu: ${w}x${h}" }

        if (w == target && h == target) return bitmap

        val shortSide = minOf(w, h).toFloat()
        val scale = target.toFloat() / shortSide
        // toInt yerine ceil ile garanti target'tan büyük/eşit
        val newW = ((w * scale) + 0.5f).toInt().coerceAtLeast(target)
        val newH = ((h * scale) + 0.5f).toInt().coerceAtLeast(target)

        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        val cropX = ((newW - target) / 2).coerceAtLeast(0)
        val cropY = ((newH - target) / 2).coerceAtLeast(0)

        // Crop dikkatli: sınırı aşmasın
        val safeW = (newW - cropX).coerceAtMost(target)
        val safeH = (newH - cropY).coerceAtMost(target)
        if (safeW != target || safeH != target) {
            Log.w(TAG, "Crop boyutu beklenenden küçük: ${safeW}x${safeH}, hedef ${target}x${target}")
            // Fallback: doğrudan stretch
            return Bitmap.createScaledBitmap(bitmap, target, target, true)
        }

        return Bitmap.createBitmap(scaled, cropX, cropY, target, target)
    }

    private fun bitmapToNchw(bitmap: Bitmap): FloatBuffer {
        floatBuffer.rewind()
        bitmap.getPixels(pixelBuffer, 0, inputSize, 0, 0, inputSize, inputSize)
        val channelSize = inputSize * inputSize

        if (useImageNetNormalize) {
            // timm/Swin/ConvNeXt: (pixel/255 − mean) / std
            // ImageNet stats: mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]
            val invStdR = 1f / IMAGENET_STD_R
            val invStdG = 1f / IMAGENET_STD_G
            val invStdB = 1f / IMAGENET_STD_B
            for (i in 0 until channelSize) {
                val p = pixelBuffer[i]
                val r = ((p shr 16) and 0xFF) / 255f
                val g = ((p shr 8) and 0xFF) / 255f
                val b = (p and 0xFF) / 255f
                floatBuffer.put(i, (r - IMAGENET_MEAN_R) * invStdR)
                floatBuffer.put(i + channelSize, (g - IMAGENET_MEAN_G) * invStdG)
                floatBuffer.put(i + 2 * channelSize, (b - IMAGENET_MEAN_B) * invStdB)
            }
        } else {
            // Ultralytics YOLO-cls: sadece [0,1] normalize
            for (i in 0 until channelSize) {
                val p = pixelBuffer[i]
                floatBuffer.put(i, ((p shr 16) and 0xFF) / 255f)                       // R
                floatBuffer.put(i + channelSize, ((p shr 8) and 0xFF) / 255f)          // G
                floatBuffer.put(i + 2 * channelSize, (p and 0xFF) / 255f)              // B
            }
        }

        floatBuffer.rewind()
        return floatBuffer
    }

    override fun close() {
        runCatching { session.close() }
        runCatching { env.close() }
    }

    companion object {
        private const val TAG = "YoloOnnxRunner"

        // ImageNet preprocessing sabitleri (timm/Swin/ConvNeXt için)
        private const val IMAGENET_MEAN_R = 0.485f
        private const val IMAGENET_MEAN_G = 0.456f
        private const val IMAGENET_MEAN_B = 0.406f
        private const val IMAGENET_STD_R  = 0.229f
        private const val IMAGENET_STD_G  = 0.224f
        private const val IMAGENET_STD_B  = 0.225f

        // Sınıf sırası (Ultralytics + timm alfabetik — eğitimle aynı)
        private val ULTRA_ORDER: List<LesionClass> = listOf(
            LesionClass.AKIEC,   // 0
            LesionClass.BCC,     // 1
            LesionClass.BKL,     // 2
            LesionClass.DF,      // 3
            LesionClass.MEL,     // 4
            LesionClass.NV,      // 5
            LesionClass.VASC     // 6
        )
        private const val ULTRA_MEL_IDX = 4
    }
}

/**
 * YOLO11-cls çıkarım sonucu.
 *
 * @property primaryClass Threshold-tuning sonrası nihai sınıf.
 * @property confidence   Sınıfın olasılık değeri.
 * @property boundingBox  Classification modelinde lokalizasyon yoktur → daima null.
 * @property topKProbabilities Tüm 7 sınıfın olasılıkları, azalan sırada.
 * @property inferenceTimeMs Inference gecikmesi (milisaniye).
 */
data class YoloPrediction(
    val primaryClass: LesionClass,
    val confidence: Float,
    val boundingBox: BBox?,
    val topKProbabilities: List<Pair<LesionClass, Float>>,
    val inferenceTimeMs: Long
)
