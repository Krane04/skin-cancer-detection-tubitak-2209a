package com.example.melonomscanner.ml

import android.graphics.Bitmap
import com.example.melonomscanner.data.model.BBox
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Görüntü kalitesi değerlendirmesi.
 *
 * Dermoskopik fotoğrafların klinik kullanıma uygun olması için üç kritere bakılır:
 *  1) Bulanıklık (Laplacian varyansı) — odaklanma yeterli mi?
 *  2) Parlaklık — ne çok karanlık ne çok yanık.
 *  3) Lezyon doluluk oranı — lezyon çerçevede yeterince büyük görünüyor mu?
 *
 * Sonuç: [QualityReport] ve kullanıcıya gösterilecek Türkçe ipuçları.
 */
object QualityChecker {

    // Eşikler deneysel — HAM10000 benzeri dermoskopi çekimlerinde iyi çalışır.
    private const val BLUR_THRESHOLD = 55.0          // Laplacian varyansı altında => bulanık
    private const val BRIGHTNESS_MIN = 40.0          // 0..255
    private const val BRIGHTNESS_MAX = 220.0
    private const val FILL_RATIO_MIN = 0.08f         // lezyon çerçevenin en az %8'i olmalı
    private const val FILL_RATIO_MAX = 0.85f         // çok yakınsa lens distorsiyonu olabilir
    private const val DOWNSAMPLE_SIZE = 160          // Laplacian/brightness için küçültme

    data class QualityReport(
        val blurScore: Double,
        val brightness: Double,
        val fillRatio: Float?,
        val isBlurry: Boolean,
        val isTooDark: Boolean,
        val isTooBright: Boolean,
        val isLesionTooSmall: Boolean,
        val isLesionTooClose: Boolean,
        val hints: List<String>
    ) {
        val isAcceptable: Boolean
            get() = !isBlurry && !isTooDark && !isTooBright &&
                    !isLesionTooSmall && !isLesionTooClose

        /** 0..1 arası toplam kalite puanı (UI göstergesinde kullanılabilir). */
        val overallScore: Float
            get() {
                var score = 1f
                if (isBlurry) score -= 0.4f
                if (isTooDark || isTooBright) score -= 0.25f
                if (isLesionTooSmall) score -= 0.2f
                if (isLesionTooClose) score -= 0.15f
                return score.coerceIn(0f, 1f)
            }
    }

    fun evaluate(bitmap: Bitmap, lesionBox: BBox? = null): QualityReport {
        val small = if (bitmap.width > DOWNSAMPLE_SIZE || bitmap.height > DOWNSAMPLE_SIZE) {
            val scale = DOWNSAMPLE_SIZE.toFloat() / max(bitmap.width, bitmap.height)
            val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else bitmap

        val gray = toGrayscale(small)
        val blur = laplacianVariance(gray, small.width, small.height)
        val brightness = averageBrightness(gray)

        val fillRatio = lesionBox?.let { computeFillRatio(it) }

        val isBlurry = blur < BLUR_THRESHOLD
        val isTooDark = brightness < BRIGHTNESS_MIN
        val isTooBright = brightness > BRIGHTNESS_MAX
        val isSmall = fillRatio != null && fillRatio < FILL_RATIO_MIN
        val isClose = fillRatio != null && fillRatio > FILL_RATIO_MAX

        val hints = buildList {
            if (isBlurry) add("Görüntü bulanık. Telefonu sabit tutun ve odaklanmasını bekleyin.")
            if (isTooDark) add("Işık yetersiz. Daha aydınlık bir ortama geçin veya flaşı açın.")
            if (isTooBright) add("Parlaklık çok yüksek. Doğrudan yansımayı azaltın.")
            if (isSmall) add("Lezyon çok küçük görünüyor. Biraz daha yaklaşın.")
            if (isClose) add("Çok yakınsınız. Lens kenarları lezyonu deforme ediyor.")
            if (isEmpty()) add("Görüntü kalitesi iyi. Çekebilirsiniz.")
        }

        return QualityReport(
            blurScore = blur,
            brightness = brightness,
            fillRatio = fillRatio,
            isBlurry = isBlurry,
            isTooDark = isTooDark,
            isTooBright = isTooBright,
            isLesionTooSmall = isSmall,
            isLesionTooClose = isClose,
            hints = hints
        )
    }

    // ---- Private helpers ----

    private fun toGrayscale(bitmap: Bitmap): IntArray {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val gray = IntArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            // Rec. 601 luma
            gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }
        return gray
    }

    private fun averageBrightness(gray: IntArray): Double {
        if (gray.isEmpty()) return 0.0
        var sum = 0L
        for (v in gray) sum += v
        return sum.toDouble() / gray.size
    }

    /**
     * 3x3 Laplacian çekirdeği uygulayıp varyansını döndürür.
     * Varyans yüksekse görüntü keskin, düşükse bulanık demektir.
     */
    private fun laplacianVariance(gray: IntArray, w: Int, h: Int): Double {
        if (w < 3 || h < 3) return 0.0
        val size = (w - 2) * (h - 2)
        val out = DoubleArray(size)
        var idx = 0
        var mean = 0.0
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val c = gray[y * w + x]
                val top = gray[(y - 1) * w + x]
                val bot = gray[(y + 1) * w + x]
                val left = gray[y * w + (x - 1)]
                val right = gray[y * w + (x + 1)]
                val lap = (4 * c - top - bot - left - right).toDouble()
                out[idx] = lap
                mean += lap
                idx++
            }
        }
        mean /= size
        var variance = 0.0
        for (v in out) {
            val d = v - mean
            variance += d * d
        }
        return variance / size
    }

    private fun computeFillRatio(box: BBox): Float {
        // BBox girdisi zaten 640x640 giriş uzayında; [0..640] piksel ya da normalize olabilir.
        val w = abs(box.right - box.left)
        val h = abs(box.bottom - box.top)
        val area = w * h
        val frameArea = 640f * 640f
        // Eğer normalize (0..1) verilmişse frame area 1 olur, bu durumda area'yı olduğu gibi dön.
        val ratio = if (area <= 1.5f) area else area / frameArea
        return ratio.coerceIn(0f, 1f)
    }

    // Test/debug için — bir Bitmap'in Laplacian varyansını tek çağrıda dönersin.
    @Suppress("unused")
    fun standaloneBlurScore(bitmap: Bitmap): Double {
        val gray = toGrayscale(bitmap)
        return laplacianVariance(gray, bitmap.width, bitmap.height)
    }

    // Kullanılmasa da dış kullanım için bırakıldı.
    @Suppress("unused")
    private fun stddev(values: DoubleArray): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        var sum = 0.0
        for (v in values) sum += (v - mean) * (v - mean)
        return sqrt(sum / values.size)
    }

    @Suppress("unused")
    private fun clamp(v: Double, lo: Double, hi: Double) = min(hi, max(lo, v))
}
