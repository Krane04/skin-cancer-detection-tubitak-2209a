package com.example.melonomscanner.data.model

/**
 * YOLO modelinin cilt lezyonu için döndürdüğü 7-sınıf etiketleri.
 * Projede HAM10000 taksonomisi (7 sınıf) kullanılır.
 *
 * Her sınıf için aşağıdaki bilgiler tutulur:
 *  - code: Ultralytics/ISIC kısaltması (eğitimle aynı sıra)
 *  - label: Türkçe tıbbi ad
 *  - isMalignant: kötü huylu (malign) mu?
 *  - shortName: kullanıcıya kısa adıyla (örn. "Ben", "Melanom") sunulan ad
 *  - userExplanation: Lezyonun ne olduğunun 1-2 cümlelik halk dili açıklaması
 *  - typicalAppearance: Tipik görsel özellikleri (kullanıcı ayna karşısında karşılaştırabilir)
 *  - clinicalAction: Kullanıcının ne yapması gerektiği
 *  - baselineRisk: Bu sınıfın tahmin edilmesi durumundaki varsayılan risk seviyesi
 *                  (gerçek risk, model güvenine ve threshold'a göre RiskClassifier'da güncellenebilir)
 */
enum class LesionClass(
    val code: String,
    val label: String,
    val isMalignant: Boolean,
    val shortName: String,
    val userExplanation: String,
    val typicalAppearance: String,
    val clinicalAction: String,
    val baselineRisk: RiskLevel,
) {
    NV(
        code = "nv",
        label = "Melanositik Nevüs (Ben)",
        isMalignant = false,
        shortName = "Ben",
        userExplanation =
            "Halk arasında 'ben' olarak bilinen iyi huylu lezyondur. " +
            "Bebeklikten itibaren oluşabilir; çoğu insanda 10-40 arası bulunur ve genelde zararsızdır.",
        typicalAppearance =
            "Simetrik şekil, düzgün kenarlar, tek-tip kahverengi/siyah renk, genellikle 6 mm'nin altında çap.",
        clinicalAction =
            "Şüpheli değişim (büyüme, kenar bozulması, renk değişikliği, kanama) görmediğiniz sürece rutin kontrol yeterlidir.",
        baselineRisk = RiskLevel.LOW,
    ),
    MEL(
        code = "mel",
        label = "Melanom",
        isMalignant = true,
        shortName = "Melanom",
        userExplanation =
            "Cilt kanseri türleri arasında en agresif olanıdır. Tüm cilt kanseri kaynaklı ölümlerin yaklaşık %75'inden sorumludur. " +
            "Erken yakalanırsa 5 yıllık sağkalım %99'dur; gecikirse bu oran %27'ye düşer.",
        typicalAppearance =
            "ABCDE: Asimetri, Düzensiz kenar (Border), Renk değişkenliği (Color), 6 mm üzeri çap (Diameter), Zamanla değişim (Evolving).",
        clinicalAction =
            "BİR HAFTA İÇİNDE bir dermatoloğa başvurun. Erken müdahale hayat kurtarır. Çekilen fotoğrafı dermatologa gösterebilirsiniz.",
        baselineRisk = RiskLevel.CRITICAL,
    ),
    BKL(
        code = "bkl",
        label = "Seboreik Keratoz",
        isMalignant = false,
        shortName = "Yaşlılık Beni",
        userExplanation =
            "İyi huylu, yaşla birlikte oluşan kahverengi-siyah kabarık lezyondur. " +
            "Genellikle 40 yaş üstünde ortaya çıkar ve kanser riski taşımaz. 'Yağ lekesi' veya 'yaşlılık beni' olarak bilinir.",
        typicalAppearance =
            "Kabarık, mumsu/yapışkan görünümlü yüzey, kahverengi-siyah renk, sınırları belirgin.",
        clinicalAction =
            "Genelde tedaviye gerek yoktur. Estetik kaygı veya tahriş varsa dermatologdan dondurma (kriyoterapi) veya küretaj istenebilir.",
        baselineRisk = RiskLevel.LOW,
    ),
    BCC(
        code = "bcc",
        label = "Bazal Hücreli Karsinom",
        isMalignant = true,
        shortName = "Cilt Kanseri (BCC)",
        userExplanation =
            "En sık görülen cilt kanseri türüdür ama melanomdan çok daha az ölümcüldür. " +
            "Genellikle güneş gören bölgelerde (yüz, kulak, boyun) oluşur; yavaş büyür, çok nadir metastaz yapar.",
        typicalAppearance =
            "İnci tanesi gibi parlak, üzerinde küçük damarcıklar görülen pembe-kırmızı çıkıntı; merkezi çukurlaşabilir veya kabuk bağlayabilir.",
        clinicalAction =
            "ÖNÜMÜZDEKİ HAFTA bir dermatoloğa başvurun. Erken müdahale ile %95 oranında tam iyileşme sağlanır; cerrahi eksizyon en yaygın tedavidir.",
        baselineRisk = RiskLevel.HIGH,
    ),
    AKIEC(
        code = "akiec",
        label = "Aktinik Keratoz / Bowen Hastalığı",
        isMalignant = true,
        shortName = "Güneş Hasarı (Pre-kanser)",
        userExplanation =
            "Yıllarca güneşe maruz kalmış ciltte oluşan, kanser öncüsü bir durumdur. " +
            "İşlem yapılmazsa %5-10 oranında skuamöz hücreli cilt kanserine dönüşebilir.",
        typicalAppearance =
            "Kırmızı-pembe zeminde kuru, pürüzlü, zımpara hissi veren küçük yamalar; yüz, kulak, eller ve saçsız kafa derisinde tipiktir.",
        clinicalAction =
            "1-2 AY İÇİNDE dermatoloğa başvurun. Erken tedavide topikal krem, kriyoterapi veya foto-dinamik tedavi yeterlidir.",
        baselineRisk = RiskLevel.HIGH,
    ),
    VASC(
        code = "vasc",
        label = "Vasküler Lezyon",
        isMalignant = false,
        shortName = "Damar Lezyonu",
        userExplanation =
            "Cildin altındaki kılcal damarların genişlemesi veya birikmesinden oluşan iyi huylu lezyonlardır. " +
            "Yenidoğan benleri (hemanjiom), kiraz hemanjiomları ve telangiektaziler bu grupta yer alır.",
        typicalAppearance =
            "Kırmızı-mor renkli, basıldığında rengi kısmen solan, düzgün sınırlı küçük çıkıntı veya yama.",
        clinicalAction =
            "Genelde tedaviye gerek yoktur. Estetik kaygı varsa veya boyut büyüyorsa lazer tedavisi için dermatologdan görüş alınabilir.",
        baselineRisk = RiskLevel.LOW,
    ),
    DF(
        code = "df",
        label = "Dermatofibroma",
        isMalignant = false,
        shortName = "Cilt Düğümü",
        userExplanation =
            "Genellikle bacaklarda görülen, sert ve düğüm şeklinde iyi huylu bir lezyondur. " +
            "Çoğu zaman böcek ısırığı veya küçük bir travmadan sonra oluşur; kanser değildir.",
        typicalAppearance =
            "Sert, kabarık, kahverengi-pembe küçük düğüm; iki taraftan sıkıştırıldığında merkezi içe çöker ('çukurlaşma belirtisi').",
        clinicalAction =
            "Tedavi genelde gerekmez. Şüpheli görünüyorsa veya kaşıntı/ağrı varsa dermatologa danışılabilir.",
        baselineRisk = RiskLevel.LOW,
    );

    companion object {
        fun fromIndex(index: Int): LesionClass = entries.getOrElse(index) { NV }
        fun fromCode(code: String): LesionClass? = entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

/** Tek bir tarama sonucunun nihai hali (Result ekranında gösterilir, DB'ye yazılır) */
data class ScanOutcome(
    val primary: LesionClass,
    val confidence: Float, // 0..1
    val topKProbabilities: List<Pair<LesionClass, Float>>,
    val riskLevel: RiskLevel,
    val boundingBox: BBox?,
    val heatmapPath: String? = null,
    val inferenceTimeMs: Long,
    val usedMetadata: Boolean
)

data class BBox(val left: Float, val top: Float, val right: Float, val bottom: Float)
