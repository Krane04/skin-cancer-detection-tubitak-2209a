# MelonomScanner — Android Uygulaması

Cilt lezyonu ön değerlendirmesi için akıllı telefon uygulaması. Kotlin Jetpack Compose + ONNX Runtime Mobile ile geliştirilmiştir; **cihaz üzerinde** çalışır, internet bağlantısı gerekmez.

## Özellikler

- 🔍 **Cihaz üzerinde inference** — Swin-Tiny FP16 ONNX, ortalama 280 ms / görüntü (Snapdragon 720G)
- 📸 **CameraX** ile canlı kamera entegrasyonu + kompozisyon kılavuzu
- 🩺 **Görüntü kalite kontrolü** — bulanıklık (Laplacian varyansı), aydınlatma (Rec. 601 luma), lezyon doluluk oranı
- 📊 **Sınıf-özelinde açıklamalar** — Her 7 sınıf için Türkçe halk dili açıklama + risk seviyesi + klinik eylem önerisi
- 💾 **Yerel Room veritabanı** — Tahmin geçmişi, KVKK uyumlu (tüm veriler cihazda kalır)
- 🎨 **Material 3** tasarım — Splash animasyonu, ABCDE kuralı rehberi, geçmiş listesi
- 🌐 **Türkçe arayüz** — Tıbbi terminoloji halk diliyle açıklanmıştır

## Gereksinimler

- Android Studio Hedgehog (2023.1.1) veya üstü
- JDK 17
- Android SDK 33+
- Minimum Android API 26 (Android 8.0 Oreo)
- Hedef Android API 34 (Android 14)

## Kurulum

### 1. Model dosyasını indir

```bash
cd ../models
bash download_models.sh
cp swin_tiny_fp16.onnx ../android/app/src/main/assets/best.onnx
```

Veya YOLO11 FP16 yedeği kullanmak istersen:
```bash
cp yolo11m_cls_fp16.onnx ../android/app/src/main/assets/best.onnx
```

> **Not:** `app/src/main/assets/best.onnx` repo'ya dahil edilmemiştir (~55 MB).
> İlk derlemede mutlaka indirip kopyalaman gerekir.

### 2. Build

```bash
./gradlew assembleDebug
# APK çıktısı: app/build/outputs/apk/debug/app-debug.apk
```

veya Android Studio'da: **Build → Make Project**

### 3. Cihaza yükle

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Proje Yapısı

```
android/
├── app/
│   ├── build.gradle.kts                Modul build script
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   └── best.onnx               Swin-Tiny FP16 (manuel kopyalanır)
│       ├── res/                        Drawable, layout, themes, strings
│       │   ├── drawable/
│       │   │   ├── ic_launcher_*.xml   Logo (lezyon + tarama köşeleri)
│       │   │   └── ic_splash_logo.xml
│       │   └── values/
│       │       ├── colors.xml          Teal-mavi brand paleti
│       │       └── strings.xml         Türkçe metinler
│       └── java/com/example/melonomscanner/
│           ├── MainActivity.kt
│           ├── MelonomScannerApp.kt    Singleton DI container
│           ├── data/
│           │   ├── db/                 Room veritabanı
│           │   ├── model/              LesionClass enum + ScanOutcome
│           │   └── repository/         ScanRepository
│           ├── ml/
│           │   ├── YoloOnnxRunner.kt   ONNX Runtime adapter (Swin + YOLO)
│           │   ├── QualityChecker.kt   Görüntü kalite kontrolü
│           │   └── RiskClassifier.kt   Tahmin → RiskLevel haritalama
│           └── ui/
│               ├── navigation/         Navigation Compose
│               ├── screens/
│               │   ├── splash/         Animated brand splash
│               │   ├── onboarding/
│               │   ├── consent/        KVKK + tıbbi uyarı
│               │   ├── home/
│               │   ├── metadata/       Hasta bilgileri formu
│               │   ├── camera/
│               │   ├── result/         Tahmin + açıklamalar + olasılık
│               │   ├── history/        Room'dan tahmin geçmişi
│               │   ├── info/           ABCDE kuralı rehberi
│               │   └── settings/
│               ├── components/         Reusable UI components
│               └── theme/              Material 3 teması
└── gradle/libs.versions.toml           Version catalog
```

## Mimari Kararlar

### ONNX Runtime Mobile entegrasyonu

`YoloOnnxRunner.kt` hem Ultralytics YOLO hem timm Swin/ConvNeXt modelleriyle çalışacak şekilde tasarlanmıştır:

- **YOLO modelleri için:** Sadece `[0,1]` normalize
- **timm modelleri için:** ImageNet normalize `(pixel/255 − mean) / std`

Bu davranış `useImageNetNormalize: Boolean` parametresi ile kontrol edilir:

```kotlin
val runner = YoloOnnxRunner(
    context = ctx,
    modelAssetName = "best.onnx",
    useImageNetNormalize = true  // Swin için TRUE, YOLO için FALSE
)
```

### Melanom-odaklı threshold tuning

ONNX modeli softmax olasılıkları döndürür; karar kuralı:

```kotlin
if (probs[MEL_IDX] >= melThreshold) {
    return LesionClass.MEL          // Mel-öncelikli
} else {
    return ULTRA_ORDER[argmax]      // Argmax tabanlı
}
```

Önerilen threshold değerleri (Swin için):
- `0.20` → Tarama senaryosu (mel recall ~0.92)
- `0.30` → Karar destek (mel recall ~0.88) ← **varsayılan**
- `0.45` → Yüksek-precision (mel recall ~0.82)

### Görüntü kalite kontrolü

`QualityChecker.kt` üç metrik üzerinden değerlendirme yapar:

| Metrik | Eşik | Hata mesajı |
|---|---|---|
| Bulanıklık (Laplacian var.) | ≥ 55 | "Görüntü bulanık. Telefonu sabit tutun..." |
| Aydınlatma (luma) | 40-220 | "Işık yetersiz" / "Parlaklık çok yüksek" |
| Lezyon doluluk oranı | %8-85 | "Lezyon çok küçük" / "Çok yakınsınız" |

## Tıbbi Uyarı

> ⚠ Bu uygulama bir **araştırma prototipi**dir. CE/FDA onaylı bir tıbbi cihaz değildir.
> Sonuçlar yalnızca ön değerlendirme niteliğindedir. Şüpheli her lezyon için mutlaka bir dermatoloğa başvurun.

## KVKK Uyumluluğu

- 📵 Tüm veri yerel olarak cihazda kalır
- 🌐 İnternet izni talep edilmez
- 🗑 Kullanıcı istediği zaman Ayarlar → "Verilerimi Sil" ile tüm tahmin geçmişini silebilir
- 📋 Aydınlatma metni ilk açılışta gösterilir, kabul edilmeden uygulama kullanılamaz

## Bilinen Sınırlamalar

1. **Eğitim verisi biası:** Model çoğunlukla beyaz-cilt (Fitzpatrick I-III) örnekleriyle eğitilmiştir. Koyu ciltli (Fitzpatrick IV-VI) hastalarda doğruluk düşebilir.
2. **Bulanık görüntü:** Telefon kameralarının dermoskopi adaptörü olmadan çekilen görüntüleri pürüzlü olabilir; özel dermoskopi adaptörü önerilir.
3. **DF ve VASC sınıfları:** Test setinde n < 200 olduğundan bu iki sınıf için tahminlere düşük güvenle yaklaşılmalıdır.

## Tezdeki referans bölümler

- Bölüm 2.3 — Yazılım Mimarisi (Compose UI + ML pipeline)
- Bölüm 4.8 — ONNX FP16 conversion ve mobil deployment
- Bölüm 4.8.1 — Mobil model seçimi: klinik-öncelikli deployment
