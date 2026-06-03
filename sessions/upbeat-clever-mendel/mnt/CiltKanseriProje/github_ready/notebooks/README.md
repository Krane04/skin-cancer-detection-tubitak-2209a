# Colab Notebook'ları

Bu klasör, projenin tüm deneylerini içeren Google Colab notebook'larını barındırır. Her notebook bağımsızdır ve sırayla çalıştırılması zorunlu değildir.

## Önerilen çalışma sırası

| # | Notebook | Çıktı | Süre (A100) |
|---|---|---|---|
| 01 | `01_yolo_karsilastirma.ipynb` | YOLOv8m-cls + YOLO11m-cls eğitim | ~16 saat |
| 02 | `02_yolo26_ekleme.ipynb` | YOLO26m-cls eğitim + 3-YOLO ensemble | ~8 saat |
| 03 | `03_multimodal_fusion.ipynb` | FT-Transformer multimodal model | ~6 saat |
| 04 | `04_segmentation_crop_retrain.ipynb` | YOLO11n-detect + cropla yeniden eğitim | ~10 saat |
| 05 | `05_diversity_ensemble.ipynb` | ConvNeXt + Swin + Diverse-5 ensemble + ONNX | ~10 saat |

## Google Colab'da Açma

Her notebook için, GitHub'daki dosyaya tıklayıp "Open in Colab" buton kullanılabilir veya:

```
https://colab.research.google.com/github/<kullanici-adin>/skin-cancer-detection-tubitak-2209a/blob/main/notebooks/<NOTEBOOK_ADI>.ipynb
```

## Bağımlılıklar

Her notebook ilk hücresinde gerekli paketleri kurar. Lokal çalıştırma için kök dizindeki `requirements.txt` yeterlidir.

## Drive entegrasyonu

Notebook'lar `/content/drive/MyDrive/Colab Notebooks/CiltKanseri/CiltKanseriProje/` yolunu varsayar. Kendi Drive yapınızla uyumlu çalışmak için `PROJECT_ROOT` değişkenini kendi yolunuza güncelleyin.
