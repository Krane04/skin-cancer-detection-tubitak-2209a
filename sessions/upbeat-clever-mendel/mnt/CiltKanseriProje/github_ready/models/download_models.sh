#!/usr/bin/env bash
# =============================================================================
# Skin Cancer Detection — Model dosyalarını Hugging Face Hub'dan indir
# =============================================================================
set -euo pipefail

HF_REPO="${HF_REPO:-Krane04/skin-cancer-isic-yolo-convnext-swin}"

if ! command -v huggingface-cli &> /dev/null; then
    echo "huggingface_hub kurulu değil. pip install huggingface_hub çalıştır."
    echo "  pip install -U huggingface_hub"
    exit 1
fi

echo "============================================================"
echo "  Model dosyaları indiriliyor..."
echo "  Kaynak: $HF_REPO"
echo "  Hedef:  $(pwd)"
echo "============================================================"

huggingface-cli download "$HF_REPO" \
    --include "*.pt" "*.onnx" \
    --local-dir . \
    --local-dir-use-symlinks False

echo ""
echo "İndirilen dosyalar:"
ls -lh *.pt *.onnx 2>/dev/null | awk '{print "  " $NF " (" $5 ")"}'

echo ""
echo "Toplam disk kullanımı:"
du -sh . 2>/dev/null | awk '{print "  " $1}'
