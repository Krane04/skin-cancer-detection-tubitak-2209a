package com.example.melonomscanner.ml

import com.example.melonomscanner.data.model.BodyRegion
import com.example.melonomscanner.data.model.FitzpatrickType
import com.example.melonomscanner.data.model.LesionClass
import com.example.melonomscanner.data.model.PatientMetadata
import com.example.melonomscanner.data.model.RiskLevel
import com.example.melonomscanner.data.model.ScanOutcome

/**
 * YOLO çıktısını + hasta metadata'sını birleştirerek klinik risk seviyesine dönüştürür.
 *
 * Not: Bu sınıf FT-Transformer'ın Android tarafındaki **kural tabanlı fallback** rolünü
 * üstlenir. Eğer ileride ONNX olarak FT-Transformer ağı da gömülürse, skorlaması buradan
 * ağırlıklı olarak uygulanabilir. Şu an için:
 *  - Sınıfın malign olup olmaması,
 *  - Güven skoru,
 *  - Yaş, Fitzpatrick, bölge, lezyon boyutu, aile öyküsü faktörleri
 *  kullanılarak ponderleme yapılır.
 */
object RiskClassifier {

    private const val WEIGHT_BASE = 0.55f
    private const val WEIGHT_METADATA = 0.45f

    fun classify(prediction: YoloPrediction, metadata: PatientMetadata): ScanOutcome {
        val baseScore = baseScoreForClass(prediction.primaryClass, prediction.confidence)
        val metadataScore = metadataScore(metadata, prediction.primaryClass)

        val combined = (WEIGHT_BASE * baseScore + WEIGHT_METADATA * metadataScore)
            .coerceIn(0f, 1f)

        val risk = scoreToRisk(combined, prediction.primaryClass)

        return ScanOutcome(
            primary = prediction.primaryClass,
            confidence = prediction.confidence,
            topKProbabilities = prediction.topKProbabilities,
            riskLevel = risk,
            boundingBox = prediction.boundingBox,
            heatmapPath = null,
            inferenceTimeMs = prediction.inferenceTimeMs,
            usedMetadata = true
        )
    }

    /**
     * Sınıfın kendi başına getirdiği risk ağırlığı.
     *  MEL ve BCC yüksek, AKIEC orta-yüksek, DF/NV düşük sayılır.
     */
    private fun baseScoreForClass(cls: LesionClass, confidence: Float): Float {
        val malignancyWeight = when (cls) {
            LesionClass.MEL   -> 1.0f
            LesionClass.BCC   -> 0.85f
            LesionClass.AKIEC -> 0.70f
            LesionClass.BKL   -> 0.35f
            LesionClass.DF    -> 0.20f
            LesionClass.VASC  -> 0.20f
            LesionClass.NV    -> 0.10f
        }
        // Güvenin kareköküyle hafifletelim — düşük güvenlerde abartılı ceza vermeyelim.
        val confFactor = Math.sqrt(confidence.toDouble()).toFloat()
        return (malignancyWeight * confFactor).coerceIn(0f, 1f)
    }

    private fun metadataScore(meta: PatientMetadata, cls: LesionClass): Float {
        var score = 0f
        var maxWeight = 0f

        // Yaş: 50+ melanoma riskini artırır
        when {
            meta.age >= 70 -> { score += 0.9f; maxWeight += 1f }
            meta.age >= 50 -> { score += 0.6f; maxWeight += 1f }
            meta.age >= 30 -> { score += 0.3f; maxWeight += 1f }
            else           -> { score += 0.1f; maxWeight += 1f }
        }

        // Fitzpatrick I-II (açık tenli) melanomaya daha eğilimli
        when (meta.fitzpatrick) {
            FitzpatrickType.TYPE_I, FitzpatrickType.TYPE_II   -> { score += 0.9f; maxWeight += 1f }
            FitzpatrickType.TYPE_III                          -> { score += 0.5f; maxWeight += 1f }
            FitzpatrickType.TYPE_IV                           -> { score += 0.3f; maxWeight += 1f }
            FitzpatrickType.TYPE_V, FitzpatrickType.TYPE_VI   -> { score += 0.2f; maxWeight += 1f }
        }

        // Yüksek riskli bölgeler (baş/boyun, sırt, alt bacak)
        val region = meta.region
        val regionRisk = when (region) {
            BodyRegion.HEAD, BodyRegion.NECK, BodyRegion.SCALP,
            BodyRegion.BACK_UPPER, BodyRegion.BACK_LOWER,
            BodyRegion.LEG_LOWER -> 0.8f
            BodyRegion.CHEST, BodyRegion.ABDOMEN,
            BodyRegion.ARM_UPPER, BodyRegion.ARM_LOWER,
            BodyRegion.LEG_UPPER -> 0.5f
            BodyRegion.HAND, BodyRegion.FOOT, BodyRegion.GENITAL -> 0.4f
        }
        score += regionRisk; maxWeight += 1f

        // Lezyon boyutu: 6mm+ ABCDE'nin D kriteri
        meta.lesionSizeMm?.let { size ->
            val sizeRisk = when {
                size >= 10f -> 0.9f
                size >= 6f  -> 0.7f
                size >= 3f  -> 0.4f
                else        -> 0.2f
            }
            score += sizeRisk; maxWeight += 1f
        }

        // Aile öyküsü
        if (meta.familyHistory) { score += 0.8f; maxWeight += 0.5f }

        // Malign sınıf tahmini + yüksek riskli bölge kombinasyonu ekstra ağırlık
        if (cls.isMalignant && regionRisk >= 0.8f) {
            score += 0.5f; maxWeight += 0.5f
        }

        return if (maxWeight > 0f) score / maxWeight else 0f
    }

    private fun scoreToRisk(score: Float, cls: LesionClass): RiskLevel {
        // Malign sınıflar en azından MEDIUM sayılır
        val floor = if (cls.isMalignant) RiskLevel.MEDIUM else RiskLevel.LOW

        val base = when {
            score >= 0.80f -> RiskLevel.CRITICAL
            score >= 0.60f -> RiskLevel.HIGH
            score >= 0.35f -> RiskLevel.MEDIUM
            else           -> RiskLevel.LOW
        }
        return if (base.ordinal < floor.ordinal) floor else base
    }
}
