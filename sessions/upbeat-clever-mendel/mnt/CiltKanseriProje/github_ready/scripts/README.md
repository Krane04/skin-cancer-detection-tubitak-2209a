# Yardımcı Scriptler

Bu klasör notebook'lar dışında lokal CLI üzerinden kullanılabilecek scriptleri içerir.

## İçerik

| Script | Açıklama |
|---|---|
| (önerilen) `prepare_dataset.py` | ISIC + HAM10000 indirip birleştirir; bkz. `../data/prepare_dataset.py` |
| (önerilen) `train_yolo.py` | Ultralytics YOLO sınıflandırma eğitim wrapper |
| (önerilen) `train_timm.py` | timm modeli (ConvNeXt, Swin) eğitim wrapper |
| (önerilen) `export_onnx.py` | PyTorch checkpoint → ONNX FP32 → ONNX FP16 |
| (önerilen) `evaluate.py` | Test seti üzerinde tüm metrik hesaplama (McNemar dahil) |

## Notebook'lardan üretim

Her notebook'un içindeki anahtar kod parçaları bu klasörde standalone Python scriptlerine dönüştürülebilir. Örnek dönüşüm:

```bash
# Notebook'tan script üret
jupyter nbconvert --to script notebooks/05_diversity_ensemble.ipynb \
                  --output scripts/diversity_ensemble
```

## Çalıştırma örneği

```bash
# Veri seti hazırla
python ../data/prepare_dataset.py --download

# YOLO11m-cls eğit
python train_yolo.py --model yolo11m-cls --epochs 50 --imgsz 384

# Swin-Tiny eğit
python train_timm.py --model swin_tiny_patch4_window7_224.ms_in22k_ft_in1k --epochs 25

# ONNX FP16 export
python export_onnx.py \
    --checkpoint ../models/swin_tiny_best.pt \
    --model swin_tiny_patch4_window7_224.ms_in22k_ft_in1k \
    --fp16 \
    --output ../models/swin_tiny_fp16.onnx

# Değerlendirme
python evaluate.py --model ../models/swin_tiny_fp16.onnx --split test
```

## Tasarım kararı

Bu projede asıl deneyler **Google Colab notebook'ları** üzerinden yürütülmüştür çünkü:

1. A100 GPU'larına ücretsiz/uygun ücretle erişim
2. Drive entegrasyonu ile veri + model arşivleme
3. Notebook hücreleri arası hızlı iterasyon

Standalone scriptler **lokal GPU sahibi** veya **HPC kullanıcıları** için sunulmuştur. Sonuçlar her iki kanaldan da aynı olmalıdır.
