# Eğitilmiş Model Dosyaları

Bu klasör, eğitilmiş model checkpoint'lerinin **link bilgilerini** içerir. Model dosyaları boyut nedeniyle (toplam ~600 MB) Git'e doğrudan yüklenmemiştir; **Hugging Face Hub**'da host edilmektedir.

## 🤗 Hugging Face Hub

📦 **Repository:** `https://huggingface.co/Krane04/skin-cancer-isic-yolo-convnext-swin`

> Linki güncellemek için bu satırı kendi HF kullanıcı adınla değiştir.

## İçerik

| Dosya | Boyut | Açıklama |
|---|---|---|
| `yolov8m_cls_best.pt` | 52 MB | YOLOv8m-cls Ultralytics checkpoint |
| `yolo11m_cls_best.pt` | 41 MB | YOLO11m-cls Ultralytics checkpoint |
| `yolo26m_cls_best.pt` | 41 MB | YOLO26m-cls Ultralytics checkpoint |
| `convnext_tiny_best.pt` | 106 MB | ConvNeXt-Tiny timm checkpoint |
| `swin_tiny_best.pt` | 105 MB | Swin-Tiny timm checkpoint |
| `multimodal_v2_best.pt` | 95 MB | YOLO11 backbone + FT-Transformer |
| `yolo11m_cls_fp16.onnx` | 20.8 MB | **Mobil — YOLO11 FP16 yedek** |
| `convnext_tiny_fp16.onnx` | 53.2 MB | Cloud / ensemble FP16 |
| `swin_tiny_fp16.onnx` | 55.4 MB | **Mobil — birincil model** |

## Hızlı İndirme

```bash
cd models/
bash download_models.sh
```

Veya Python ile:

```python
from huggingface_hub import snapshot_download

snapshot_download(
    repo_id="Krane04/skin-cancer-isic-yolo-convnext-swin",
    local_dir="./models",
    allow_patterns=["*.pt", "*.onnx"],
)
```

## Model Performansları (Test Seti, n = 3,684)

| Model | Accuracy | Macro F1 | Mel Recall | Cohen κ |
|---|---|---|---|---|
| YOLOv8m-cls | 0.7723 | 0.6929 | 0.6462 | 0.6673 |
| YOLO11m-cls | 0.7709 | 0.6763 | 0.6743 | 0.6665 |
| YOLO26m-cls | 0.7728 | 0.6565 | 0.6603 | 0.6683 |
| ConvNeXt-Tiny | 0.7326 | 0.6412 | 0.7944 | 0.6198 |
| **Swin-Tiny** | 0.6444 | 0.6076 | **0.8812** | 0.5242 |
| 3-YOLO ensemble | 0.7866 | 0.6817 | 0.6794 | 0.6880 |
| **Diverse-5 ensemble** | **0.7953** | **0.7012** | 0.7484 | **0.7030** |
| **Diverse-5 @ θ=0.24** | 0.7552 | 0.6715 | **0.8723** | 0.6536 |

## Eğitim Konfigürasyonu

Modeller aşağıdaki konfigürasyonla eğitilmiştir:

- **YOLO modelleri:** Ultralytics 8.3.x, 50 epoch, AdamW, lr0=1e-3, cos_lr, AMP, batch=128, imgsz=384
- **timm modelleri (ConvNeXt + Swin):** timm 1.0.x, 25 epoch, AdamW, lr=3e-4, OneCycleLR, AMP, batch=64, imgsz=384
- **Class weights:** Ters-frekans × 1.5 mel boost
- **Label smoothing:** 0.05
- **Augmentation:** RandAugment + MixUp 0.1 + RandomErasing 0.4 + HSV jitter + flip (yatay+dikey)

Detaylar: [`docs/BITIRME_TASARIM_TEZI.pdf`](../docs/BITIRME_TASARIM_TEZI.pdf) Bölüm 3.2-3.3.

## Lisans

Bu model checkpoint'leri eğitim verilerinden türetilmiştir; orijinal veri kümelerinin lisans şartlarına tabidir:

- ISIC 2019 / 2020 — CC BY-NC 4.0 (sadece akademik kullanım)
- HAM10000 — CC BY-NC 4.0

Modelleri ticari amaçla kullanmadan önce orijinal veri seti lisanslarını kontrol ediniz.
