# Skin Cancer Detection — TÜBİTAK 2209-A

> **Yapay Zekâ Temelli Mobil Cilt Kanseri Tarama Sistemi**
> YOLO Tabanlı Çoklu Mimari Ensemble ile Dermoskopik Görüntü Sınıflandırması

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![TÜBİTAK 2209-A](https://img.shields.io/badge/TÜBİTAK-2209--A-blue)]()
[![Python 3.10+](https://img.shields.io/badge/python-3.10+-blue.svg)](https://www.python.org/)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9+-purple.svg)](https://kotlinlang.org/)

---

## Özet

Bu proje, TÜBİTAK 2209-A Üniversite Öğrencileri Araştırma Projeleri Destek Programı kapsamında geliştirilmiştir. Akıllı telefon kameralarıyla çekilen dermoskopik benzeri görüntüleri **cihaz üzerinde** sınıflandırarak cilt lezyonu ön değerlendirmesi yapan bir mobil sistem sunar.

**Bilimsel katkılar:**

1. **YOLO ailesinin sınıflandırma varyantlarının** (YOLOv8m-cls, YOLO11m-cls, YOLO26m-cls) dermoskopi üzerinde **ilk sistematik karşılaştırması**.
2. **Diverse-5 ensemble** (3 YOLO + ConvNeXt-Tiny + Swin-Tiny) ile melanom recall'unun **0.6743 → 0.8723** seviyesine çıkarılması (TÜBİTAK 2209-A `mel recall ≥ 0.85` hedefi karşılandı).
3. **İki negatif bilimsel bulgu**: (i) hasta metadata multimodal füzyon p=0.94 ile anlamsız çıktı; (ii) detection-tabanlı kropla yeniden eğitim performansı %16.4 düşürdü. Bu sonuçlar "mimari-aile-içi doygunluk rejimi" hipotezi altında birleştirildi.
4. **Yüksek-anlamlı mimari-aileler-arası McNemar farkları** (YOLO vs ConvNeXt p<10⁻⁴, YOLO vs Swin p<10⁻⁴): bir mimari ailesi içinde plato olsa da farklı inductive bias'a sahip ailelerin ensemble'a gerçek bilgi katkısı sağladığı kanıtlandı.
5. **ONNX FP16** ile mobil deployment (Swin-Tiny 55.4 MB, mel recall 0.8812 tek-model) + Android **Jetpack Compose** prototip.

---

## Sonuçlar

### Final TÜBİTAK 2209-A Hedef Karşılaştırması

| Hedef | Eşik | En İyi Tek | 3-YOLO Ens | Diverse-5 Ens | Diverse-5 @ θ=0.24 | Sonuç |
|---|---|---|---|---|---|---|
| Mel Recall | ≥ 0.85 | 0.6743 | 0.6819 | 0.7484 | **0.8723** | **Karşılandı** |
| Latency ≤ 500 ms | — | — | — | — | ~280 ms (Swin) | **Karşılandı** |
| Mobil ≤ 25 MB | — | — | — | — | 55.4 MB (Swin) | Klinik öncelik için feda edildi |
| Doğruluk | ≥ 0.90 | 0.7734 | 0.7870 | **0.7953** | 0.7552 | Veri tavanı (~0.85 SOTA) |
| Macro F1 | ≥ 0.85 | 0.6933 | 0.6821 | **0.7012** | 0.6715 | Sınıf dengesizliği |
| Cohen κ | ≥ 0.85 | 0.6693 | 0.6886 | **0.7030** | 0.6536 | Acc'a bağlı |

### Beş-Model İkili McNemar Testleri (10 pair, Bonferroni α=0.005)

| Karşılaştırma | p-değeri | Anlamlı? |
|---|---|---|
| YOLOv8 vs YOLO11 | 0.86 | Hayır |
| YOLOv8 vs YOLO26 | 0.96 | Hayır |
| YOLO11 vs YOLO26 | 0.77 | Hayır |
| YOLO vs **ConvNeXt** | < 10⁻⁴ | **Evet** |
| YOLO vs **Swin** | < 10⁻⁴ | **Evet** |
| ConvNeXt vs Swin | < 10⁻⁴ | Evet |

➜ Doygunluk rejimi **mimari aile içinde** geçerli, **aileler arasında değil.**

---

## Repository Yapısı

```
.
├── README.md                       Proje vitrini (bu dosya)
├── LICENSE                         MIT
├── CITATION.cff                    Akademik atıf
├── requirements.txt                Python bağımlılıkları
├── .gitignore
│
├── docs/                           Tez ve raporlar
│   ├── BITIRME_TASARIM_TEZI.pdf    53 sayfa, ISE401 formatında
│   ├── BITIRME_OZET.pdf            Özet formatı
│   └── BITIRME_ARA_RAPORU.md       Detaylı interim rapor
│
├── notebooks/                      Google Colab notebook'ları
│   ├── 01_yolo_karsilastirma.ipynb     YOLOv8/v11 karşılaştırma
│   ├── 02_yolo26_ekleme.ipynb          YOLO26 ekleme (Ocak 2026)
│   ├── 03_multimodal_fusion.ipynb      FT-Transformer multimodal
│   ├── 04_segmentation_crop_retrain.ipynb   Crop+retrain pipeline
│   └── 05_diversity_ensemble.ipynb     ConvNeXt + Swin + ensemble
│
├── figures/                        Tezdeki tüm PNG figürler
│
├── data/                           Veri seti hazırlığı
│   ├── README.md                   ISIC + HAM10000 indirme talimatları
│   ├── prepare_dataset.py          Birleştirme + patient-level split
│   └── splits/                     Hazır train/val/test CSV'leri
│
├── models/                         Eğitilmiş model dosyaları
│   └── README.md                   Hugging Face Hub linkleri
│
├── android/                        MelonomScanner Android app
│   ├── README.md
│   ├── app/                        Kotlin Compose kaynak kodu
│   └── build.gradle.kts
│
└── scripts/                        Yardımcı scriptler
    ├── train_yolo.py               YOLO eğitim
    ├── train_timm.py               ConvNeXt/Swin eğitim
    ├── export_onnx.py              FP16 ONNX export
    └── evaluate.py                 Test seti değerlendirme
```

---

## Kurulum

### 1. Repo'yu klonla

```bash
git clone https://github.com/Krane04/skin-cancer-detection-tubitak-2209a.git
cd skin-cancer-detection-tubitak-2209a
```

### 2. Python ortamı

```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

### 3. Veri setini indir

```bash
cd data
python prepare_dataset.py --download   # ISIC 2019+2020+HAM10000, ~9.5 GB
```

Detaylar için: [`data/README.md`](data/README.md)

### 4. Model dosyalarını indir

Eğitilmiş model checkpoint'leri (~600 MB) Hugging Face Hub'da host edilmektedir:

🤗 https://huggingface.co/Krane04/skin-cancer-isic-yolo-convnext-swin

```bash
cd models
bash download_models.sh
```

Detaylar için: [`models/README.md`](models/README.md)

### 5. Android uygulamasını derle

```bash
cd android
./gradlew assembleDebug
# APK çıktısı: app/build/outputs/apk/debug/app-debug.apk
```

Detaylar için: [`android/README.md`](android/README.md)

---

## Çalıştırma

### Eğitim (Google Colab tavsiye edilir — A100 GPU)

Her bir notebook bağımsız olarak çalıştırılabilir:

| Notebook | Süre (A100) | Çıktı |
|---|---|---|
| 01_yolo_karsilastirma | 24 saat | 3 YOLO checkpoint + metrikler |
| 02_yolo26_ekleme | 8 saat | YOLO26 checkpoint |
| 03_multimodal_fusion | 6 saat | FT-Transformer model + sonuçlar |
| 04_segmentation_crop_retrain | 10 saat | YOLO11n-detect + cropped cls |
| 05_diversity_ensemble | 10 saat | ConvNeXt + Swin + final ensemble |

### Komut satırından (lokal GPU ile)

```bash
# YOLO11m-cls eğit
python scripts/train_yolo.py --model yolo11m-cls --data /path/to/cls_data

# ConvNeXt-Tiny eğit (timm)
python scripts/train_timm.py --model convnext_tiny.fb_in22k_ft_in1k_384

# ONNX FP16 export
python scripts/export_onnx.py --checkpoint models/swin_tiny_best.pt --fp16

# Test seti değerlendirme
python scripts/evaluate.py --model models/swin_tiny_fp16.onnx --split test
```

---

## Atıf

Bu çalışmayı akademik bir yayında kullanırsanız lütfen şu şekilde atıf yapınız:

```bibtex
@misc{aktunc2026skincancer,
  author       = {Aktunç, Erkan},
  title        = {Skin Cancer Detection — TÜBİTAK 2209-A:
                  Mobile Skin Cancer Screening System with YOLO and
                  Architectural Diversity Ensemble},
  year         = {2026},
  publisher    = {GitHub},
  howpublished = {\url{https://github.com/Krane04/skin-cancer-detection-tubitak-2209a}},
  note         = {TÜBİTAK 2209-A Üniversite Öğrencileri Araştırma Projesi,
                  Sakarya Üniversitesi}
}
```

Detaylı atıf bilgisi için: [`CITATION.cff`](CITATION.cff)

---

## Lisans

Bu proje **MIT Lisansı** altında dağıtılmaktadır. Detaylar için [`LICENSE`](LICENSE) dosyasına bakınız.

> **Tıbbi Uyarı:** Bu yazılım bir araştırma prototipidir. CE/FDA onaylı bir tıbbi cihaz **değildir**. Sonuçlar yalnızca ön değerlendirme niteliğindedir. Şüpheli her lezyon için mutlaka bir dermatoloğa başvurun.

---

## Teşekkür

- **TÜBİTAK 2209-A** Üniversite Öğrencileri Araştırma Projeleri Destek Programı (proje desteği)
- **Sakarya Üniversitesi Bilgisayar ve Bilişim Bilimleri Fakültesi**, Bilişim Sistemleri Mühendisliği Bölümü
- Açık erişimli **ISIC 2019**, **ISIC 2020** ve **HAM10000** veri kümelerini hazırlayan araştırmacılar
- **Ultralytics** (YOLO), **timm** (Hugging Face), **PyTorch** ve **ONNX Runtime** ekipleri

---

## İletişim

**Erkan AKTUNÇ** — Sakarya Üniversitesi Bilişim Sistemleri Mühendisliği
📧 aktuncerkan04@gmail.com
