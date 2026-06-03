# Veri Seti Hazırlığı

Bu proje üç açık erişimli dermoskopi veri kümesini birleştirir. **Toplam ~9.5 GB**.

| Kaynak | Görüntü | Sınıflar | İndir |
|---|---|---|---|
| ISIC 2019 Challenge | 25,331 | 8 (UNK dışlandı) | [Link](https://challenge.isic-archive.com/data/#2019) |
| ISIC 2020 Challenge | 33,126 | MEL + NV (2,000 dengeli) | [Link](https://challenge.isic-archive.com/data/#2020) |
| HAM10000 | 10,015 | 7 (ISIC-2019 alt-küme) | [DOI](https://doi.org/10.7910/DVN/DBW86T) |
| **Birleşik (dedup sonrası)** | **25,915** | **7 sınıf** | — |

## Hızlı Başlangıç

### 1. Otomatik indirme (önerilen)

```bash
cd data/
python prepare_dataset.py --download --output-dir ./dataset_yolo
```

Bu script:
1. ISIC 2019, 2020 ve HAM10000'i resmi kaynaklardan indirir
2. 7-sınıf taksonomi (`akiec`, `bcc`, `bkl`, `df`, `mel`, `nv`, `vasc`) altında birleştirir
3. Dedup (isim + perceptual hash) uygular
4. `splits/` klasöründeki hazır train/val/test CSV'lerini kullanarak ayırır
5. `dataset_yolo/images/{train,val,test}/{class}_ISIC_xxxx.jpg` formatında kaydeder

**Beklenen süre:** ~30-60 dakika (internet hızına bağlı).
**Disk kullanımı:** ~9.5 GB.

### 2. Manuel indirme

Otomatik script çalışmazsa:

#### ISIC 2019
1. https://challenge.isic-archive.com/data/#2019 adresinden:
   - `ISIC_2019_Training_Input.zip` (~10 GB)
   - `ISIC_2019_Training_GroundTruth.csv`
2. `data/raw/ISIC_2019/` altına çıkar.

#### ISIC 2020
1. https://challenge.isic-archive.com/data/#2020 adresinden:
   - `ISIC_2020_Training_JPEG.zip` (~23 GB)
   - `ISIC_2020_Training_GroundTruth_v2.csv`
2. `data/raw/ISIC_2020/` altına çıkar.

#### HAM10000
1. https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/DBW86T
   - `HAM10000_images_part_1.zip` + `HAM10000_images_part_2.zip`
   - `HAM10000_metadata.csv`
2. `data/raw/HAM10000/` altına çıkar.

3. Sonra script'i `--download` flag'i olmadan çalıştır:
   ```bash
   python prepare_dataset.py --input-dir ./raw --output-dir ./dataset_yolo
   ```

## Sınıf Dağılımı (Birleşik Test Seti, n = 3,684)

| Kod | Türkçe Ad | n | Risk |
|---|---|---|---|
| AKIEC | Aktinik Keratoz / Bowen Hastalığı | 327 | Pre-kanser |
| BCC | Bazal Hücreli Karsinom | 514 | Kötü huylu |
| BKL | Seboreik Keratoz | 1,099 | İyi huylu |
| DF | Dermatofibroma | 115 | İyi huylu |
| MEL | Melanom | 445 | **Kötü huylu (kritik)** |
| NV | Melanositik Nevüs (Ben) | 1,113 | İyi huylu |
| VASC | Vasküler Lezyon | 77 | İyi huylu |

## Patient-Level Stratified Split

Klinik veri kümelerinde aynı hastadan farklı zaman noktalarında çekilmiş görüntüler bulunabilir. Naïf rastgele bölmede bu **data leakage**'a yol açar.

Bu projede ISIC'nin `lesion_id` ve `patient_id` alanları kullanılarak **hasta-seviyesi stratified split** uygulanmıştır:

- Eğitim: 18,207 görüntü (%70.3)
- Doğrulama: 5,644 görüntü (%21.8)
- Test: 3,690 görüntü (%14.2)

Hazır split CSV'leri `splits/` altında bulunmaktadır:
- `splits/train.csv`
- `splits/val.csv`
- `splits/test.csv`

Her CSV'nin formatı: `image_name,class_code,patient_id,lesion_id`.

## Atıflar

Bu veri kümelerini kullanırsanız orijinal yayınlara atıf yapınız:

1. **HAM10000:** Tschandl, P., Rosendahl, C., & Kittler, H. (2018). *Scientific Data*, 5, 180161.
   DOI: [10.1038/sdata.2018.161](https://doi.org/10.1038/sdata.2018.161)

2. **ISIC 2018/2019:** Codella, N. C. F. et al. (2019). *arXiv:1902.03368*.

3. **ISIC 2020:** Rotemberg, V. et al. (2021). *Scientific Data*, 8(1), 34.
   DOI: [10.1038/s41597-021-00815-z](https://doi.org/10.1038/s41597-021-00815-z)
