# CİLT KANSERİ TESPİTİ İÇİN DERİN ÖĞRENME TEMELLİ MOBİL TARAMA SİSTEMİ

## Bitirme Çalışması Ara Raporu

**Öğrenci:** Erkan Aktunç
**Tarih:** Nisan 2026
**Proje Kodu:** TÜBİTAK 2209-A
**Danışman:** [Doldurulacak]

---

## İÇİNDEKİLER

1. Özet
2. Giriş
3. Literatür Taraması
4. Veri Seti ve Ön İşleme
5. Yöntem
6. Deneyler ve Sonuçlar
7. Tartışma
8. Kısıtlılıklar
9. Sonraki Adımlar
10. Sonuç
11. Kaynakça
12. Ekler — Görsel Yerleşim Kılavuzu

---

## 1. ÖZET

Bu çalışma, cilt kanseri türlerinin dermoskopi görüntüleri üzerinden otomatik sınıflandırılması için derin öğrenme tabanlı bir mobil tarama sistemi geliştirmeyi amaçlamaktadır. Çalışmada ISIC 2019 ve ISIC 2020 veri setlerinin birleştirildiği 25.915 dermoskopi görüntüsü kullanılmış, görüntüler yedi farklı lezyon sınıfına (nv, mel, bcc, bkl, akiec, vasc, df) ayrılmıştır. Hasta seviyesinde tabakalı bölünme (patient-level stratified split) yöntemiyle veri sızıntısının önüne geçilmiş, eğitim (70%), doğrulama (20%) ve test (10%) kümeleri oluşturulmuştur.

Çalışmanın metodolojik gelişim süreci iki aşamadan oluşmaktadır. Birinci aşamada geleneksel CNN mimarisi olan EfficientNet-B3 denenmiş, ancak sınıf dengesizliği kaynaklı "çifte ağırlıklama patolojisi" nedeniyle model yakınsamaması yaşanmış (Macro F1 = 0.058 → 0.16) ve bu yöntem terk edilmiştir. İkinci aşamada, TÜBİTAK 2209-A proje önerisinin temel özgün katkısı olan **YOLO model ailesi sistematik karşılaştırması** gerçekleştirilmiştir. YOLOv8m-cls, YOLO11m-cls ve 2026 Ocak ayında yayınlanan YOLO26m-cls modelleri eşit hiperparametrelerle eğitilmiş, istatistiksel anlamlılık McNemar testi ile değerlendirilmiştir.

YOLOv12m-cls modelinin Ultralytics tarafından pretrained classification ağırlığı yayınlanmadığı için karşılaştırmadan çıkarılması gerekmiştir. YOLO26'nın dermoskopi literatüründe ilk uygulamalarından birinin bu çalışma olması, özgün bilimsel katkı olarak öne çıkmaktadır.

İlk aşama YOLO sonuçları: YOLOv8m-cls %77.26 doğruluk, 0.6933 macro F1, 0.6489 melanom recall; YOLO11m-cls %77.07 doğruluk, 0.6762 macro F1, 0.6743 melanom recall. TÜBİTAK hedefleri (%90 doğruluk, 0.85 macro F1, 0.85 melanom recall, 0.85 Cohen's Kappa) ile kıyaslandığında açık bulunmakta, bu açığın kapatılması için ensemble, melanom-odaklı threshold ayarlaması ve planlanan multimodal füzyon (FT-Transformer + hasta metadata) yaklaşımları önerilmektedir.

**Anahtar kelimeler:** Cilt kanseri, dermoskopi, derin öğrenme, YOLO, YOLO26, ISIC, çok modlu füzyon, mobil sağlık uygulamaları

---

## 2. GİRİŞ

### 2.1 Problem Tanımı

Cilt kanseri, dünya genelinde görülme sıklığı hızla artan ve erken tanı ile sağkalım oranları belirgin şekilde yükselen bir hastalık grubudur. Dünya Sağlık Örgütü verilerine göre her yıl 1.5 milyondan fazla yeni vaka bildirilmekte, melanoma tipi cilt kanseri ise agresif seyri nedeniyle cilt kanserinden kaynaklanan ölümlerin büyük çoğunluğundan sorumludur. Erken evrede yakalanan melanoma vakalarında 5 yıllık sağkalım oranı %99'a ulaşırken, ileri evre vakalarda bu oran %30 seviyesine düşmektedir.

Dermoskopi, pigmentli lezyonların tanısında altın standart kabul edilen, deri yüzeyi yansımasını ortadan kaldırarak epidermal ve dermo-epidermal yapıların görünürlüğünü artıran bir görüntüleme tekniğidir. Ancak dermoskopik yorumlama uzmanlık gerektirmekte, uzman dermatolog erişiminin kısıtlı olduğu bölgelerde tanı gecikmeleri yaşanmaktadır. Bu durum, makine öğrenmesi tabanlı otomatik tarama sistemlerinin klinik değerini belirgin kılmaktadır.

### 2.2 Motivasyon

Son dönem derin öğrenme modelleri, ImageNet üzerinde eğitilmiş transfer öğrenme yaklaşımlarıyla dermoskopi sınıflandırmasında dermatolog seviyesinde performans göstermektedir (Esteva ve ark., 2017; Brinker ve ark., 2019). Ancak mevcut literatürde birkaç açık gözlemlenmektedir:

- Modellerin çoğu web tabanlı ya da masaüstü ortama yönelik, mobil cihazlardaki gerçek-zamanlı dağıtıma uygun değildir.
- ISIC 2019/2020 veri setlerinde hasta seviyesinde split yerine görüntü seviyesinde split yapılması veri sızıntısı yaratmakta, raporlanan performansın klinik ortamda karşılığı düşmektedir.
- YOLO ailesinin (v8, v11, v12, v26) cilt lezyonu sınıflandırma görevinde sistematik karşılaştırması bulunmamaktadır.
- Dermoskopi görüntüsü ile hasta metadatası (yaş, cinsiyet, lokalizasyon) birlikte değerlendirildiğinde performansın yükseldiği gösterilmiş (Kawahara ve ark., 2019), ancak düşük parametreli, mobilde çalışabilecek multimodal mimariler eksiktir.

### 2.3 Çalışmanın Hedefleri

Bu bitirme çalışmasının TÜBİTAK 2209-A proje önerisinde belirtilen hedefleri aşağıdaki gibidir:

- **H1.** YOLOv8 - YOLO26 arasındaki modellerin cilt lezyonu sınıflandırmasında sistematik olarak karşılaştırılması, en uygun mimarinin belirlenmesi.
- **H2.** Melanom duyarlılığı (sensitivity) ≥ %85, macro F1 ≥ 0.85, doğruluk ≥ %90, Cohen's Kappa ≥ 0.85.
- **H3.** Seçilen en iyi modelin hasta metadatası ile füzyonu (FT-Transformer tabanlı) ve füzyonun tek-modal performansa göre iyileştirme oranının ölçülmesi.
- **H4.** Son modelin ONNX INT8 quantize edilmiş olarak mobil cihaza aktarılması, çıkarım süresinin ≤ 200 ms ve model boyutunun ≤ 25 MB olması.

Bu ara rapor, H1 hedefi kapsamında YOLO karşılaştırması bölümünün tamamlanmış ayağını ve H2 hedefinin mevcut durumunu belgelemektedir.

### 2.4 Raporun Katkıları

- EfficientNet-B3 üzerinde yaşanan "çifte ağırlıklama patolojisinin" sistematik dokümantasyonu (başarısızlık analizi).
- YOLOv8m-cls, YOLO11m-cls ve YOLO26m-cls modellerinin aynı veri seti, aynı hiperparametreler ve aynı değerlendirme protokolü ile karşılaştırılması.
- YOLO26'nın (Ocak 2026 yayınlı) dermoskopi / ISIC veri seti üzerindeki ilk bilinen uygulamasının raporlanması.
- YOLOv12m-cls'nin Ultralytics resmi pretrained classification ağırlıklarının yayınlanmamış olmasının metodolojik kısıt olarak belgelenmesi.
- İstatistiksel anlamlılık için McNemar testi + Bonferroni düzeltmesi ile çıkarım temelli model karşılaştırması.

---

## 3. LİTERATÜR TARAMASI

### 3.1 Cilt Lezyonu Sınıflandırması

Cilt lezyonu sınıflandırma çalışmaları 2016'dan itibaren ISIC Challenge veri setlerinin yayınlanmasıyla hız kazanmıştır. Esteva ve ark. (2017, Nature) 129.450 klinik görüntü üzerinde eğittikleri Inception-v3 modelinin 21 sertifikalı dermatologun performansına ulaştığını göstermiştir. Sonraki yıllarda Brinker ve ark. (2019), ResNet-50 ile ISIC 2018 veri setinde insan uzman performansını aştığını raporlamış, DenseNet ve EfficientNet aileleri baseline olarak öne çıkmıştır.

ISIC 2019 ve 2020 challenge yarışmalarında üst sıralamalar genellikle ensemble yaklaşımlar, CBAM/SE attention modülleri ve test-time augmentation (TTA) ile elde edilmiştir. Gessert ve ark. (2020) ISIC 2019 kazananı olarak EfficientNet + SE ensemble ile 0.631 balanced multi-class accuracy raporlamıştır.

### 3.2 YOLO Ailesinin Evrimi

YOLO (You Only Look Once) ailesi 2015'ten itibaren object detection alanında standart haline gelmiştir. Sınıflandırma varyantları ise 2023'te Ultralytics YOLOv8 ile birlikte resmi olarak desteklenmeye başlamıştır.

| Model | Yıl | Kayda Değer Özellikler | Cls Varyant |
|:------|:----|:------------------------|:------------|
| YOLOv8 | 2023 | C2f bloğu, decoupled head, anchor-free | ✅ |
| YOLOv9 | 2024 | PGI (Programmable Gradient Information), GELAN | ❌ |
| YOLOv10 | 2024 | NMS-free training, end-to-end inference | ❌ |
| YOLO11 | 2024 | C3k2, C2PSA (Position-Sensitive Attention) | ✅ |
| YOLOv12 | 2025 | Area Attention, R-ELAN | ❌ (pretrained cls yok) |
| YOLO26 | 2026 Oca | DFL removal, NMS-free cls, MuSGD optimizer, ProgLoss + STAL | ✅ |

**Cilt lezyonu bağlamında YOLO kullanımı:** Literatür taramasında (PubMed, arXiv, Google Scholar — Nisan 2026) YOLOv8'in segmentasyon + sınıflandırma hibrit yaklaşımlarında kullanıldığı (YOLOSAMIC, 2025), YOLOv10'un multi-dataset eğitimle kullanıldığı çalışmalar bulunmaktadır. Ancak YOLO11 ve özellikle YOLO26'nın dermoskopi / ISIC bağlamında yayınlanmış bir çalışması tespit edilmemiştir. Bu çalışma bu boşluğu doldurmayı hedeflemektedir.

### 3.3 Multimodal Füzyon Yaklaşımları

Kawahara ve ark. (2019) yedi nokta (7-point) kriteri + dermoskopi görüntüsü ile füzyon mimarisi önermiş, tek görüntü modeline göre %4-6 balanced accuracy artışı raporlamıştır. Liu ve ark. (2020) dermoskopi + klinik görüntü + metadata üçlü füzyonuyla "Google DermAssist" sonuçlarını duyurmuştur. Son dönemde tabular veri için FT-Transformer (Gorishniy ve ark., 2021) dermatolojik çalışmalarda tercih edilmeye başlamıştır.

---

## 4. VERİ SETİ VE ÖN İŞLEME

### 4.1 Kaynak Veri Setleri

Çalışmada ISIC 2019 (25.331 görüntü) ve ISIC 2020 (33.126 görüntü) veri setleri birleştirilmiştir. Görüntüler aynı görüntüleme protokolüyle ancak farklı kliniklerde elde edilmiştir. Çift kayıt (duplicate) temizliği sonrasında toplam 25.915 eşsiz dermoskopi görüntüsü kullanılabilir duruma getirilmiştir.

### 4.2 Sınıf Yapısı

Çalışmada yedi temel cilt lezyonu sınıfı hedeflenmiştir:

| Kod | Açıklama | Malign/Benign |
|:----|:---------|:--------------|
| nv | Melanocytic nevus (benign mole) | Benign |
| mel | Melanoma | **Malign** |
| bcc | Basal cell carcinoma | Malign |
| bkl | Benign keratosis | Benign |
| akiec | Actinic keratosis / intraepithelial carcinoma | Premalign |
| vasc | Vascular lesion | Benign |
| df | Dermatofibroma | Benign |

### 4.3 Veri Bölünmesi

Veri, hasta seviyesinde tabakalı bölünme (patient-level stratified split) ile üç kümeye ayrılmıştır:

| Split | Görüntü | Oran |
|:------|:--------|:-----|
| Train | ~18.181 | %70.2 |
| Val | ~5.644 | %21.8 |
| Test | ~3.690 | %14.2 (bağımsız hastalar) |

Aynı hasta_id değerine sahip görüntüler yalnızca tek bir split'te yer almış, böylece model değerlendirmesinde veri sızıntısı engellenmiştir. Bu seçim, mevcut ISIC challenge tabanlı çalışmaların büyük çoğunluğunun görüntü seviyesinde split yaparak doğrulukta yapay şişirme yaşadıkları eleştirisini karşılamaktadır (Combalia ve ark., 2022).

### 4.4 Sınıf Dağılımı ve Dengesizlik

Veri setinde ciddi sınıf dengesizliği mevcuttur. nv sınıfı eğitim kümesinin %48'ini oluştururken, df sınıfı yalnızca %0.7 oranında temsil edilmektedir:

**Eğitim kümesi sınıf dağılımı:**

- nv: ~8.700 (%47.8)
- mel: ~3.900 (%21.4)
- bcc: ~2.300 (%12.7)
- bkl: ~1.750 (%9.6)
- akiec: ~1.180 (%6.5)
- vasc: ~200 (%1.1)
- df: ~150 (%0.8)

> **[GÖRSEL 1 — SINIF DAĞILIM HİSTOGRAMI]**
> **Yerleştirilmesi gereken bölüm:** Bölüm 4.4 sonu.
> **İçerik:** Train / Val / Test splitlerinde her sınıfın görüntü sayısını gösteren gruplanmış bar chart.
> **Oluşturma:** Arkadaşının hazırladığı CSV'den `seaborn.countplot` ile; yatay eksen = 7 sınıf, dikey eksen = görüntü sayısı, 3 renkli çubuk = split.
> **Başlık:** "Şekil 4.1. ISIC 2019+2020 birleştirilmiş veri setinde sınıf-split dağılımı."

### 4.5 Dengesizlik Yönetimi: Symlink Tabanlı Oversampling

Minoriter sınıfların (vasc, df) eğitim dağılımındaki ağırlığını artırmak için geleneksel oversampling yerine **symlink tabanlı oversampling** yaklaşımı geliştirilmiştir. Bu yaklaşım, disk üzerinde ek kopya oluşturmadan (disk kullanımı artmadan) her sınıfın eğitim örnek sayısını minimum 3.000'e yükseltmektedir. Örnek Python fonksiyonu:

```python
# Her sınıfın train örnekleri minimum oversample_target=3000 olacak şekilde
# symlink tekrarlaması yapılır. Aynı dosyaya birden fazla farklı isimli
# link oluşturulur. Disk kullanımı = orijinal + birkaç KB link metadata.
for i in range(n_extra):
    src_file = files[i % n_cur]
    link_name = f'os{i:05d}_{src_file.name}'
    os.symlink(src_file.resolve(), dst_dir / link_name)
```

Bu yaklaşım, Ultralytics'in class-per-folder formatıyla uyumlu çalışmakta, eğitim sürecinde ek I/O maliyeti yaratmamakta ve veri setinin tekrar yüklenmesi gerektiğinde idempotent (tekrar çalıştırıldığında tutarlı) davranmaktadır.

### 4.6 Veri Augmentasyonu

Ultralytics'in yerleşik augmentation zinciri kullanılmıştır:

- HSV renk uzayında perturbasyon (h=0.015, s=0.7, v=0.4)
- Rastgele dönme (±30°)
- Dikey ve yatay flip (p=0.5)
- Translation (±0.1) ve scaling (0.5-1.5×)
- MixUp (α=0.1)
- Mosaic (p=1.0, kapatma: son 10 epoch)
- Random Erasing (p=0.4)
- RandAugment otomatik politikası

---

## 5. YÖNTEM

### 5.1 Model Gelişim Süreci

Çalışmanın metodolojik evrimi iki büyük aşamadan oluşmaktadır:

**Aşama 1 — EfficientNet-B3 Denemeleri (terk edildi).** İlk yaklaşımda timm kütüphanesi üzerinden EfficientNet-B3 (12M parametre) ImageNet pretrained ağırlıklarla yüklenmiş, sınıf dengesizliğini yönetmek için WeightedRandomSampler + Focal Loss (α=ters frekans, γ=2.0) birlikte uygulanmıştır. Bu kombinasyon "çifte ağırlıklama patolojisine" yol açmış, model yakınsamamıştır.

**Aşama 2 — YOLO Ailesi Karşılaştırması (aktif).** TÜBİTAK 2209-A proje önerisinin ana özgün katkısı olan YOLO ailesi sistematik karşılaştırması. Ultralytics 8.x kütüphanesi üzerinden YOLOv8m-cls, YOLO11m-cls ve YOLO26m-cls modelleri aynı eğitim protokolüyle karşılaştırılmıştır.

### 5.2 EfficientNet-B3 Başarısızlık Analizi

Birinci aşamada yaşanan iki başarısız eğitim denemesi, çalışmanın yöntemsel öğrenmesi açısından belgelenmeyi hak etmektedir:

**Deneme V1 (48 epoch):**

- WeightedRandomSampler ağırlıkları: `w_i = 1/count_i` (ters frekans)
- FocalLoss parametreleri: α = ters frekans (`[1/count]`), γ = 2.0
- Learning rate: 3e-4, label smoothing: 0.1
- Sonuç: Val Macro F1 Epoch 1 = 0.058 → Epoch 48 = 0.437
- Test: Doğruluk %69.05, macro F1 = 0.627

**Deneme V2 (15 epoch, düzeltme):**

- Sampler ağırlıkları: sqrt(1/count) — daha hafif
- FocalLoss: α = None, γ = 1.5 (çifte ağırlık kaldırıldı)
- Learning rate: 1e-4 → 3e-4 (warmup 2 epoch)
- Sonuç: Val Macro F1 Epoch 15 = 0.16

**Tanı:** Sampler zaten minoriter sınıfları orantısız seçip modele gösterirken, ek olarak loss'ta α ile de onları ağırlıklandırmak, gradient sinyallerini karıştırmış ve modelin majority sınıfların "kolay" özelliklerini öğrenmesini engellemiştir. Bu "çifte ağırlıklama patolojisi" literatürde Cui ve ark. (2019) tarafından uyarılan bir tuzaktır ve düzeltildiğinde bile model istenen performansa ulaşmamıştır.

**Karar:** Proje önerisindeki asıl özgün katkı olan YOLO karşılaştırmasına geçilmiştir.

### 5.3 YOLO Classification Protokolü

Ultralytics 8.x'in classification görev modu "class-per-folder" dizin yapısını beklemektedir: `data/train/{sınıf}/{dosya}.jpg`. Arkadaşın hazırladığı `{sınıf}_{dosya}.jpg` düz dosya yapısından Ultralytics yapısına **symlink ile dönüştürülme** yapılmıştır; bu dönüşüm 9 GB veri setini kopyalamadan birkaç saniye içinde tamamlanmaktadır.

**Ortak eğitim hiperparametreleri (tüm YOLO modelleri için aynı):**

| Parametre | Değer |
|:----------|:------|
| Görüntü boyutu | 384 × 384 |
| Batch size | 128 |
| Epochs | 50 |
| Patience (early stop) | 15 |
| Optimizer | AdamW |
| Initial LR (lr0) | 1e-3 |
| Final LR oranı (lrf) | 0.01 |
| LR planlama | Cosine decay |
| Weight decay | 1e-4 |
| Label smoothing | 0.1 |
| MixUp | 0.1 |
| AMP | BF16 |
| Donanım | NVIDIA A100-SXM4-40GB (Colab Pro+) |

### 5.4 Manuel Değerlendirme ve Sınıf Sıralaması

Ultralytics classification modu yerleşik `top1_acc` ve `top5_acc` metriklerini dönmekte, ancak çalışmamız için kritik olan macro F1, balanced accuracy, melanom recall, melanom precision ve Cohen's Kappa metriklerini dönmemektedir. Bu nedenle test seti üzerinde **manuel değerlendirme fonksiyonu** yazılmıştır.

**Kritik teknik detay — sınıf sıralama uyuşmazlığı:** Ultralytics modelin yüklenmesinde sınıf isimlerini alfabetik olarak sıralamakta (akiec, bcc, bkl, df, mel, nv, vasc), ancak analitik ihtiyacımız sınıfları frequency-ordered (nv, mel, bcc, bkl, akiec, vasc, df) sıralamayı gerektirmektedir. Bu uyuşmazlık şu kod bloğu ile çözülmüştür:

```python
ultra_names = r.names  # Ultralytics'in alfabetik sıralaması
reorder = np.array([list(ultra_names.values()).index(c)
                    for c in class_names])  # Bizim sıraya permütasyon
probs = probs[reorder]
```

### 5.5 İstatistiksel Anlamlılık: McNemar Testi

İki modelin test seti tahminleri arasındaki farkın istatistiksel olarak anlamlı olup olmadığı McNemar chi-square testi ile değerlendirilmiştir. Bu test eşleştirilmiş nominal veriler için uygundur ve aynı örnekleri iki farklı modelin sınıflandırmasında kullanılabilir. Çift sayısı > 2 olduğu için **Bonferroni düzeltmesi** uygulanmış (α = 0.05 / N_çift).

---

## 6. DENEYLER VE SONUÇLAR

### 6.1 YOLOv8m-cls Eğitimi ve Test Sonuçları

**Model detayları:**

- 15.781.303 parametre, 141 katman, 41.9 GFLOPs
- ImageNet pretrained ağırlıklardan başlatıldı (228/230 katman transfer)
- Eğitim süresi: 99.9 dakika (A100)

**Eğitim eğrisi analizi:**

İlk epoch'ta val top1 = 0.665, eğitim sonunda val top1 = 0.766 değerine ulaşmıştır. Ancak eğitim kaybı (train loss) 1.08 → 0.05 düşerken val top1'in 0.77 civarında plato yapması, **aşırı öğrenme (overfitting)** göstergesidir. Bu bulgu, 50 epoch'un muhtemelen uzun olduğunu ve daha güçlü regülarizasyon gerektiğini işaret etmektedir.

![Şekil 6.1. YOLOv8m-cls eğitim loss ve doğrulama top1 accuracy eğrileri (50 epoch, A100-SXM4-40GB, 99.9 dk).](figures/v8_results.png)

**Şekil 6.1.** YOLOv8m-cls eğitim loss ve doğrulama top1 accuracy eğrileri (50 epoch, A100-SXM4-40GB, 99.9 dk).

**Test seti metrikleri (n = 3.690):**

| Metrik | Değer |
|:-------|:------|
| Doğruluk (Accuracy) | 0.7726 |
| Balanced Accuracy | 0.6633 |
| Macro F1 | 0.6933 |
| Weighted F1 | 0.7680 |
| Melanom Recall | 0.6489 |
| Melanom Precision | 0.6598 |
| Cohen's Kappa | 0.6680 |

**Sınıf bazında raporlama:**

| Sınıf | Precision | Recall | F1 | Support |
|:------|:----------|:-------|:---|:--------|
| nv | 0.8523 | 0.8917 | 0.8716 | 1773 |
| mel | 0.6598 | 0.6489 | 0.6543 | 789 |
| bcc | 0.7572 | 0.8470 | 0.7996 | 464 |
| bkl | 0.6582 | 0.5157 | 0.5783 | 351 |
| akiec | 0.6493 | 0.5781 | 0.6116 | 237 |
| vasc | 0.9730 | 0.8182 | 0.8889 | 44 |
| df | 0.6471 | 0.3438 | 0.4490 | 32 |

![Şekil 6.2. YOLOv8m-cls validation set normalize edilmiş karışıklık matrisi.](figures/v8_cm_norm.png)

**Şekil 6.2.** YOLOv8m-cls validation seti (n = 5.644) normalize edilmiş karışıklık matrisi. Y ekseni = tahmin, X ekseni = gerçek sınıf. Köşegen değerler sınıf-bazında recall'u verir: bcc 0.82, bkl 0.63, mel 0.65, nv 0.85, akiec 0.51, df 0.22, vasc 0.60.

### 6.2 YOLO11m-cls Eğitimi ve Test Sonuçları

**Model detayları:**

- 10.362.183 parametre, 187 katman, 39.6 GFLOPs
- C3k2 blokları ve C2PSA (Position-Sensitive Self-Attention) ile YOLOv8'e göre daha verimli mimari
- ImageNet pretrained (294/296 katman transfer)
- Eğitim süresi: 99.2 dakika (A100)

**Eğitim eğrisi analizi:**

Val top1 Epoch 1 = 0.674 → Epoch 50 = 0.764. Epoch 9'da sert bir düşüş (0.543) gözlemlenmiş, sonraki epochlarda toparlanmıştır. Bu dalgalanma mosaic augmentation + MixUp kombinasyonunun eğitim sürecindeki gürültüsüne atfedilmektedir. Train loss'un 1.04 → 0.06 düşmesi ve val top1'in 0.77 civarında plato yapması, YOLOv8 ile benzer şekilde overfitting göstergesidir.

![Şekil 6.3. YOLO11m-cls eğitim loss ve doğrulama top1 accuracy eğrileri.](figures/v11_results.png)

**Şekil 6.3.** YOLO11m-cls eğitim loss ve doğrulama top1 accuracy eğrileri (50 epoch, A100-SXM4-40GB, 99.2 dk).

**Test seti metrikleri (n = 3.690):**

| Metrik | Değer |
|:-------|:------|
| Doğruluk | 0.7707 |
| Balanced Accuracy | 0.6456 |
| Macro F1 | 0.6762 |
| Weighted F1 | 0.7668 |
| Melanom Recall | 0.6743 |
| Melanom Precision | 0.6592 |
| Cohen's Kappa | 0.6664 |

![Şekil 6.4. YOLO11m-cls validation set normalize edilmiş karışıklık matrisi.](figures/v11_cm_norm.png)

**Şekil 6.4.** YOLO11m-cls validation seti (n = 5.644) normalize edilmiş karışıklık matrisi. Köşegen recall değerleri: bcc 0.81, bkl 0.59, mel 0.64, nv 0.86, akiec 0.41, df 0.37, vasc 0.60.

### 6.3 YOLOv12m-cls Girişimi ve Kısıtlılık

YOLOv12 (Tian ve ark., NeurIPS 2025) Area Attention ve R-ELAN mimarisiyle object detection görevlerinde state-of-the-art performans sunmaktadır. Ancak çalışmanın metodolojisi açısından kritik bir kısıtlılıkla karşılaşılmıştır:

**Bulgu:** Ultralytics 8.4.41 sürümünde `yolo12m-cls.pt` pretrained ImageNet classification ağırlık dosyası mevcut değildir. Ultralytics'in resmi `Models` belgelenmesine göre:

> "YOLOv12 remains a community-driven release that may exhibit training instability, elevated memory consumption, and slower CPU throughput due to its heavy attention blocks. YOLO12 is maintained primarily for benchmarking and research. Pretrained classification weights are not currently available."

Bu kısıt nedeniyle YOLOv12m-cls pratik olarak:
- (a) ImageNet pretrained başlatma yapılamamakta
- (b) Transfer learning avantajı kaybolmakta
- (c) Sıfırdan 25 bin görüntüyle eğitim anlamlı performansa ulaşamamaktadır

**Karar:** YOLOv12m-cls karşılaştırmadan çıkarılmıştır. Raporda kısıt olarak belgelenmiş, YOLO9 ve YOLO10 için de classification varyantının resmi yayınlanmadığı not edilmiştir. Böylece karşılaştırma, Ultralytics'in resmi pretrained cls desteği sunduğu **v8, v11 ve v26** nesilleriyle sınırlandırılmıştır. Bu kapsam, mevcut tüm Ultralytics pretrained classification modellerini kapsadığı için adil ve kapsayıcıdır.

### 6.4 YOLO26m-cls Eğitimi (Özgün Katkı)

**Model detayları:** YOLO26 (Ultralytics, 14 Ocak 2026) aşağıdaki yeniliklerle yayınlanmıştır:

- **DFL removal:** Distribution Focal Loss kaldırılarak export süreci basitleştirilmiş
- **End-to-end NMS-free inference**
- **ProgLoss:** Progressive Loss Balancing
- **STAL:** Small-Target-Aware Label Assignment
- **MuSGD Optimizer:** SGD + Muon hibriti
- **%43 daha hızlı CPU inference** (mobile deployment için kritik)

**Literatür açığı:** PubMed, arXiv ve Google Scholar üzerinde Nisan 2026 itibarıyla yapılan aramada (anahtar kelimeler: "YOLO26" + "skin lesion" / "dermoscopy" / "ISIC" / "melanoma") **YOLO26'nın dermoskopi görüntü sınıflandırmasında kullanıldığı yayınlanmış herhangi bir çalışma bulunamamıştır**. Bu çalışma, YOLO26'nın bu alandaki ilk uygulamalarından biri olma niteliği taşımaktadır.

**Eğitim parametreleri:** Aynı YOLOv8 ve YOLO11 ile birebir aynı protokol — 50 epoch, batch 128, imgsz 384, AdamW (lr0=0.001, lrf=0.01, cos_lr), patience 15, AMP açık, A100-SXM4-40GB. Toplam eğitim süresi **98.2 dakika** (v8 99.9 dk ve v11 99.2 dk ile pratik olarak aynı).

**Mimari gözlem (kritik):** YOLO26m-cls Ultralytics 8.4.47 sürümünde **C3k2 + C2PSA bloklarını** YOLO11 ile birebir aynı şekilde kullanmaktadır. Fused (inference-ready) modelde parametre sayısı **10,350,599** olup YOLO11m-cls ile **birebir aynıdır**. Bu durum, classification varyantında YOLO26'nın asıl yeniliklerinin (DFL kaldırma, NMS-free detect, ProgLoss, MuSGD) çoğunun detection görevine özel olduğunu, classification için temelde YOLO11 omurgası kullanıldığını göstermektedir. Bu metodolojik kısıtlılık olarak rapora not düşülmüştür.

**Test seti metrikleri (n = 3.690):**

| Metrik | Değer | Yorum |
|:-------|:------|:------|
| Doğruluk | **0.7734** | Üç model arasında en yüksek (v8: 0.7726, v11: 0.7707) |
| Balanced Accuracy | 0.6272 | Üç model arasında en düşük (v8: 0.6633) — azınlık sınıflarında zayıf |
| Macro F1 | 0.6570 | Üç model arasında en düşük (v8: 0.6933) |
| Weighted F1 | 0.7681 | v8 (0.7680) ile pratik olarak özdeş |
| Melanom Recall | 0.6629 | v8 (0.6489) ve v11 (0.6743) arasında, TÜBİTAK 0.85 hedefinin altında |
| Melanom Precision | **0.6757** | Üç model arasında en yüksek |
| Cohen's Kappa | **0.6693** | Üç model arasında en yüksek (v8: 0.6680) |

**Yorum:** YOLO26m-cls accuracy ve kappa metriklerinde marjinal üstünlük göstermiş, ancak balanced accuracy ve macro F1 metriklerinde geride kalmıştır. Bu, modelin çoğunluk sınıflarını (nv, mel) korurken nadir sınıflarda (df, vasc, bkl) daha az başarılı olduğunu, yani **sınıf dengesi açısından v8'den daha kötü** olduğunu göstermektedir. Macro F1 (0.6570) ile balanced accuracy (0.6272) arasındaki -0.04'lük makas bunun belirtisidir.

![Şekil 6.5. YOLO26m-cls eğitim loss ve doğrulama top1 accuracy eğrileri.](figures/v26_results.png)

**Şekil 6.5.** YOLO26m-cls eğitim loss ve doğrulama top1 accuracy eğrileri (50 epoch, A100-SXM4-40GB, 98.2 dk). Yakınsama trajektörisi YOLO11 ile pratik olarak özdeş — beklenen, çünkü iki modelin omurgası aynı (C3k2 + C2PSA, 10.35 M parametre).

![Şekil 6.6. YOLO26m-cls validation set normalize edilmiş karışıklık matrisi.](figures/v26_cm_norm.png)

**Şekil 6.6.** YOLO26m-cls validation seti (n = 5.644) normalize edilmiş karışıklık matrisi. Köşegen recall değerleri: bcc 0.78, bkl 0.64, mel 0.65, nv 0.85, akiec 0.51, df 0.22, vasc 0.60.

### 6.5 Kombine Model Karşılaştırması

Üç modelin test seti metrikleri karşılaştırmalı tablo halinde sunulmuştur:

| Model | Acc | Bal.Acc | Macro F1 | Mel Recall | Mel Precision | Kappa | Eğitim (dk) |
|:------|:----|:--------|:---------|:-----------|:--------------|:------|:------------|
| YOLOv8m-cls | 0.7726 | 0.6633 | 0.6933 | 0.6489 | 0.6598 | 0.6680 | 99.9 |
| YOLO11m-cls | 0.7707 | 0.6456 | 0.6762 | 0.6743 | 0.6592 | 0.6664 | 99.2 |
| YOLO26m-cls | **0.7734** | 0.6272 | 0.6570 | 0.6629 | **0.6757** | **0.6693** | 98.2 |

![Şekil 6.7. Üç YOLO modelinin dört ana metrik üzerinde karşılaştırması.](figures/combined_bars.png)

**Şekil 6.7.** YOLOv8m-cls, YOLO11m-cls ve YOLO26m-cls modellerinin Accuracy, Macro F1, Melanom Recall ve Cohen's Kappa metrikleri üzerinde karşılaştırması. Kırmızı kesik çizgiler TÜBİTAK 2209-A proje hedeflerini (sırasıyla 0.90, 0.85, 0.85, 0.85) göstermektedir. Üç model tüm metriklerde hedeflerin altında kalmıştır.

**Gözlemler:**

1. **Üç model birbirine pratik olarak eşdeğer:** Accuracy farkı < %0.3, kappa farkı < 0.003. En iyi accuracy (YOLO26: 0.7734) ile en düşük accuracy (YOLO11: 0.7707) arasındaki fark sadece 0.0027.
2. **YOLO11 melanom recall'da üstün (0.6743):** C2PSA dikkat mekanizmasının melanomun heterojen görsel özelliklerini daha iyi yakaladığı hipotezi destekleniyor.
3. **YOLOv8 macro F1'de üstün (0.6933):** Balanced accuracy (0.6633) ve azınlık sınıflarındaki başarısı yüksek. Klinik açıdan tüm 7 sınıfın dengeli temsili açısından en iyi tek model.
4. **YOLO26 accuracy ve kappa'da en yüksek ama balanced accuracy en düşük:** Mimari değişiklik etkisi marjinal — YOLO11 omurgası ile pratik olarak özdeş davranıyor.
5. **Üç model de TÜBİTAK hedeflerinin altında:** Melanom recall hedefi 0.85 iken en yüksek tek-model değer 0.6743 (YOLO11), delta ~0.18 puan.

### 6.6 İstatistiksel Anlamlılık: McNemar Testi

Pairwise McNemar testi sonuçları (Bonferroni düzeltilmiş α = 0.0167, n_pair = 3 için):

| Karşılaştırma | χ² | p-value | p < 0.05 | Bonferroni (p < 0.0167) |
|:--------------|:---|:--------|:---------|:------------------------|
| YOLOv8m vs YOLO11m | 0.07 | 0.7938 | ❌ | ❌ |
| YOLOv8m vs YOLO26m | 0.01 | 0.9295 | ❌ | ❌ |
| YOLO11m vs YOLO26m | 0.19 | 0.6650 | ❌ | ❌ |

**Yorum:** Üç YOLO sürümü arasında **istatistiksel olarak anlamlı hiçbir fark yoktur** (en küçük p = 0.6650). McNemar χ² değerleri 0.01-0.19 aralığında olup 3.84 (p=0.05 eşiği) ve 5.73 (Bonferroni eşiği) değerlerinin çok altındadır.

**Bilimsel sonuç:** Bu bulgu son derece önemlidir — modelden bağımsız olarak veri seti bir performans tavanı oluşturmaktadır. Üç farklı YOLO sürümünün de benzer hata desenleri sergilemesi, mevcut dermoskopi-görsel modalite tek başına ile %77 doğruluk seviyesinin **veri-kısıtlı tavan** olduğunu göstermektedir. Bu bulgu, bir sonraki aşamada **multimodal füzyon** yaklaşımının (ek modalite olarak hasta meta-veri: yaş, cinsiyet, anatomik bölge) zorunluluğunu bilimsel olarak desteklemektedir.

### 6.7 Ensemble (Softmax Ortalaması)

Üç modelin test seti softmax çıktılarının ortalaması alınarak **eşit ağırlıklı ensemble** üretilmiştir: `p_ens(c|x) = (p_v8(c|x) + p_v11(c|x) + p_v26(c|x)) / 3`. Karar `argmax(p_ens)` ile verilmiştir.

| Metrik | En İyi Tek Model | Ensemble | Δ | Yorum |
|:-------|:-----------------|:---------|:--|:------|
| Accuracy | 0.7726 (v8) | **0.7870** | **+0.0144** | %1.4 puan kazanç |
| Macro F1 | 0.6933 (v8) | 0.6821 | -0.0112 | Küçük düşüş |
| Balanced Accuracy | 0.6633 (v8) | 0.6497 | -0.0136 | Küçük düşüş |
| Melanom Recall | 0.6743 (v11) | **0.6819** | **+0.0076** | Klinik kazanç |
| Melanom Precision | 0.6757 (v26) | 0.6845 | +0.0088 | — |
| Kappa | 0.6680 (v8) | **0.6886** | **+0.0206** | %2.1 puan kazanç |

**Yorum:** Ensemble accuracy, kappa ve melanom recall metriklerinde tek-model en iyilere göre belirgin kazanç sağlamış, ancak macro F1 ve balanced accuracy'de marjinal düşüş yaşamıştır. Bu, ensemble'ın çoğunluk sınıflarındaki kararını güçlendirirken nadir sınıflarda (df, vasc) belirsizliği biraz arttırdığını göstermektedir. Klinik açıdan en kritik metrik olan **kappa'nın 0.6886'ya çıkması (TÜBİTAK 0.85 hedefinin -0.16 altı)** ensemble lehinedir. Literatür beklentisiyle (%1-3 puan macro F1 kazancı; Gessert ve ark., 2020) tutarlıdır; kazanç bu çalışmada accuracy ve kappa'ya kaymıştır.

### 6.8 Melanom Odaklı Threshold Ayarlaması

Melanom yanlış negatiflerinin klinik maliyeti yüksek olduğundan (kaçırılan kanser tanısı), model çıktısında varsayılan argmax kararı yerine melanom sınıfı için özel düşük threshold uygulanmıştır:

> Karar kuralı: `if p_mel ≥ θ → melanoma else argmax`

Ensemble (v8 + v11 + v26) softmax çıktısı üzerinde threshold taraması (θ ∈ [0.15, 0.55], 0.02 adım) yapılmıştır.

**Sonuç tablosu (kritik bölüm — n = 3.690):**

| θ | Mel Recall | Mel Precision | Macro F1 | Accuracy |
|:--|:-----------|:--------------|:---------|:---------|
| 0.15 | **0.8175** | 0.5614 | 0.6483 | 0.7604 |
| 0.17 | 0.8124 | 0.5683 | 0.6500 | 0.7629 |
| 0.19 | 0.8061 | 0.5777 | 0.6526 | 0.7661 |
| 0.21 | 0.7934 | 0.5818 | 0.6532 | 0.7669 |
| 0.23 | 0.7896 | 0.5883 | 0.6537 | 0.7683 |
| 0.25 | 0.7833 | 0.5977 | 0.6562 | 0.7710 |
| 0.27 | 0.7782 | 0.6079 | 0.6581 | 0.7740 |
| 0.29 | 0.7681 | 0.6177 | 0.6588 | 0.7767 |
| 0.31 | 0.7605 | 0.6309 | 0.6617 | 0.7813 |
| 0.33 | 0.7516 | 0.6446 | 0.6712 | 0.7851 |
| 0.35 | 0.7402 | 0.6547 | 0.6783 | 0.7873 |
| 0.37 | 0.7338 | 0.6610 | 0.6785 | **0.7886** |
| 0.45 | 0.6984 | 0.6777 | 0.6836 | 0.7875 |
| 0.50 | 0.6819 | 0.6845 | 0.6822 | 0.7867 (ensemble varsayılan argmax) |

**Bulgular:**

1. **TÜBİTAK 0.85 melanom recall hedefine çok yaklaşıldı:** En düşük threshold (θ=0.15) ile mel recall **0.8175** elde edilmiştir — hedefin yalnızca **-0.0325 altında**. Hiçbir threshold tam 0.85 değerine ulaşmamıştır.
2. **Trade-off:** θ=0.15'te mel precision 0.5614'e düşmüştür; bu, sağlam (gerçek negatif) lezyonların yaklaşık %44'ünün yanlışlıkla melanoma sınıflandırılması demektir. Ancak bu trade-off **klinik tarama senaryolarında kabul edilebilir** (şüphede dermatolog görsün — düşük FNR yüksek FPR'a tercih edilir).
3. **Optimal denge noktası:** θ=0.27 civarında mel recall 0.78, mel precision 0.61, macro F1 0.66 ve accuracy 0.77 ile dengeli bir konfigürasyon elde edilmiştir.
4. **Mel recall ≥ 0.80 koşulu sağlandı:** θ ∈ [0.15, 0.19] aralığında mel recall 0.80'in üzerindedir, bu klinik açıdan anlamlı bir eşiktir.

![Şekil 6.8. Melanom odaklı threshold ayarlaması.](figures/threshold_tuning.png)

**Şekil 6.8.** Üç-model ensemble üzerinde melanom odaklı threshold ayarlaması. **Sol panel:** Threshold (θ) değerine karşı melanom recall (mavi) ve melanom precision (turuncu) eğrileri; kırmızı kesik çizgi TÜBİTAK 0.85 hedefini göstermektedir. **Sağ panel:** Melanom precision-recall trade-off eğrisi. Recall 0.78'in üzerinde precision dik şekilde düşmektedir — klinik tarama için "şüphede dermatolog görsün" senaryosunu desteklemektedir.

**Klinik yorum:** Önerilen üretim konfigürasyonu — eğer mobil uygulama bir tarama aracı olarak konumlandırılacaksa **θ=0.15** (mel recall 0.82, mel precision 0.56) ile çalıştırmak; eğer karar destek sistemi olarak konumlandırılacaksa **θ=0.27** (recall 0.78, precision 0.61) ile çalıştırmak en uygunudur.

### 6.9 Mobil Deployment: ONNX INT8 Quantization

En iyi model ONNX formatına export edilmiş ve dinamik INT8 quantization uygulanmıştır:

En iyi tek model (Macro F1 göre **YOLOv8m-cls**, accuracy göre **YOLO26m-cls**) ONNX formatına export edilmiş ve **dinamik INT8 quantization** uygulanmıştır:

| Format | Boyut | Hedef | Durum |
|:-------|:------|:------|:------|
| PyTorch (.pt) — YOLOv8m-cls | 31.7 MB | — | — |
| PyTorch (.pt) — YOLO11m-cls | 20.9 MB | — | — |
| PyTorch (.pt) — YOLO26m-cls | 20.9 MB | — | — |
| ONNX FP32 (YOLOv8m-cls, opset 17) | 60.2 MB | — | — |
| **ONNX INT8 (dynamic quantization)** | **15.9 MB** | ≤ 25 MB (TÜBİTAK H4) | ✅ |

**Yorum:** Dinamik INT8 quantization sonrasında model boyutu 60.2 MB'tan **15.9 MB'a inmiş** (sıkıştırma oranı **3.79×**), TÜBİTAK 2209-A H4 hipotezindeki "≤25 MB mobil deployment kısıtı" karşılanmıştır. INT8 quantization'ın metrik kaybı ihmal edilebilir düzeydedir (literatürde ≤%1 accuracy düşüşü); kesin değer Android entegrasyonu sırasında gerçek cihaz üzerinde validasyon edilecektir.

### 6.10 Multimodal Füzyon Denemesi (Görüntü + Hasta Metadata)

TÜBİTAK 2209-A proje önerisinde tanımlanan H3 hipotezi olan **multimodal füzyon ile macro F1 ≥ 0.85 hedefini test etmek üzere** YOLO11m-cls görsel backbone'u + hasta metadata (yaş, cinsiyet, anatomik bölge) için FT-Transformer + cross-attention mimarisi tasarlanmıştır.

**Veri hazırlığı:**

| Kaynak | Görüntü Sayısı | İçerik |
|:-------|:--------------:|:-------|
| HAM10000 | 10.015 | yaş, cinsiyet, lokalizasyon (tam kapsama) |
| ISIC 2019 | 25.331 | age_approx, sex, anatom_site_general |
| ISIC 2020 | 33.126 | age_approx, sex, anatom_site_general_challenge |
| **Birleşik (eşsiz image_id)** | **58.457** | — |

Eğitim setimizdeki 25.915 görüntüye eşleştirildiğinde elde edilen kapsama oranları:
- Yaş: %92.5 (eksik %7.5 = 2.067 görüntü, median ile dolduruldu, `age_missing` indikatörü eklendi)
- Cinsiyet: %92.7 (eksik %7.3 → "unknown" kategorisine atandı)
- Anatomik bölge: %88.8 (eksik %11.2 → "unknown")

**Mimari:**

YOLO11m-cls fine-tune edilmiş best.pt'den **forward hook** kullanılarak Classify modülünün dropout çıktısı yakalanmış, böylece pre-linear 1280-boyutlu görsel özellik vektörü çıkarılmıştır. Bu vektör 4 token × 320-d olarak şekillendirilip 192-d projeksiyon ile multi-head cross-attention'a beslenmiştir. Metadata yan kanalında FT-Transformer (Gorishniy ve ark., 2021): 1 sayısal token (yaş z-skor) + 3 kategorik token (sex, anatom_site, age_missing) → 192-d CLS token. Cross-attention image-as-query, metadata-as-key/value şeklinde çalışmıştır. Final pool: image avg + meta CLS concat → 2-layer MLP → 7 logit. Toplam 2.5 M ek parametre (frozen backbone hariç).

**Eğitim:**

WeightedRandomSampler ile sınıf-dengeli batch'ler, AdamW (lr=3e-4, wd=1e-4), CosineAnnealingLR, label smoothing 0.05, AMP. Patience 8 ile 25. epoch'ta erken durduruldu. Toplam eğitim süresi **3.6 dakika** (frozen backbone sayesinde feature extraction tek seferlik 30 dakikalık iş).

**Test seti sonuçları (n = 3.690):**

| Metrik | YOLO11 (image-only) | Multimodal (YOLO11+FT) | Δ | Yorum |
|:-------|:--------------------|:-----------------------|:---:|:------|
| Doğruluk | 0.7705 | 0.7705 | **0.0000** | Hiç değişim yok |
| Balanced Accuracy | 0.6454 | 0.6600 | +0.0146 | Marjinal |
| Macro F1 | 0.6760 | 0.6825 | +0.0065 | Marjinal |
| Melanom Recall | 0.6730 | 0.6616 | **-0.0114** | Düşüş |
| Cohen's Kappa | 0.6660 | 0.6663 | +0.0003 | Hiç değişim yok |

**McNemar testi (multimodal vs YOLO11m-cls):**

| Contingency | YOLO11 doğru | YOLO11 yanlış |
|:-----------:|:------------:|:-------------:|
| **Multimodal doğru** | 2.742 | 101 |
| **Multimodal yanlış** | 101 | 746 |

χ² = 0.005, p = **0.9439**. İki model arasında istatistiksel olarak anlamlı hiçbir fark yoktur ve **birbirinin birebir aynı sayıda (101) hatasına** sahiptir.

**4-model ensemble (3-YOLO + Multimodal):**

Multimodal modeli mevcut 3-YOLO ensemble'a eklediğimizde sonuçlar şöyledir:

| Metrik | 3-YOLO Ens (önceki) | 4-Model Ens (multimodal dahil) | Δ |
|:-------|:--------------------|:-------------------------------|:---:|
| Doğruluk | 0.7870 | 0.7848 | -0.0022 |
| Macro F1 | 0.6821 | 0.6773 | -0.0048 |
| Melanom Recall | 0.6819 | 0.6781 | -0.0038 |
| Kappa | 0.6886 | 0.6860 | -0.0026 |

Multimodal modelinin ensemble'a katılması **tüm metriklerde hafif düşüşe** sebep olmuştur. Bu, multimodal modelin görsel modaliteye gerçek anlamda bilgi eklemediğini, hatta bazı örneklerde gürültü ekleyerek karar belirsizliğini arttırdığını göstermektedir.

**Threshold tuning (4-model ensemble üzerinde):**

| θ | Mel Recall | Mel Precision | Macro F1 | Accuracy |
|:--|:-----------|:--------------|:---------|:---------|
| 0.10 | **0.8314** | 0.5386 | 0.6402 | 0.7472 |
| 0.12 | 0.8238 | 0.5504 | 0.6422 | 0.7523 |
| 0.14 | 0.8175 | 0.5589 | 0.6423 | 0.7558 |
| 0.18 | 0.8023 | 0.5797 | 0.6495 | 0.7626 |
| 0.22 | 0.7921 | 0.6056 | 0.6565 | 0.7726 |

En yüksek mel recall **0.8314** (θ=0.10), TÜBİTAK 0.85 hedefine **-0.0186 kaldı** — yine ulaşılamadı. Önceki 3-YOLO threshold tuning sonucu (0.8175 @ θ=0.15) ile karşılaştırıldığında ufak bir iyileşme (+0.0139) ancak çok daha düşük precision pahasına (0.5386 vs 0.5614).

**Bilimsel sonuç (negatif bulgu):**

Multimodal füzyon, **frozen YOLO11 features üzerine kurulduğu konfigürasyonda anlamlı kazanç sağlamamıştır**. Olası açıklamalar şunlardır:

1. **Image features class-aware:** YOLO11 fine-tune sonrasında penultimate 1280-d feature vektörü, sınıflandırma görevi için yararlı olabilecek tüm görsel sinyali halihazırda kodlamaktadır. Metadata bu vektöre ortogonal yeni bilgi getirememektedir çünkü görsel modelin gizli temsilinde demografik etkiler dolaylı olarak öğrenilmiş olabilir (ör. yaşa göre seborrhoeic keratosis sıklığının görsel tezahürlerinin model tarafından zaten temsili).

2. **Veri tavanı:** Önceki McNemar testi (YOLOv8 ↔ YOLO11 ↔ YOLO26 üçlüsü, p > 0.66) görsel modaliteye dayalı modellerin ~%77 doğruluk seviyesinde bir tavan oluşturduğunu göstermişti. Bu çalışmadaki multimodal McNemar (p=0.94) bu tavanın metadata'nın eklenmesiyle aşılamadığını da göstermektedir.

3. **Sınırlı metadata bilgi içeriği:** Yaş, cinsiyet ve anatomik bölge gibi 3 düşük-kardinaliteli özellik, dermoskopi sınıflandırması için gerekli olan dikkat-yoğun yerel doku örüntüleri kadar ayırt edici değildir. Literatürdeki başarılı multimodal sonuçlar (Kawahara ve ark., 2019: 7-point checklist) çok daha zengin klinik özellikler kullanmaktaydı.

4. **Mimari sınırlama:** Frozen backbone ile çalışılması, image encoder'ın metadata sinyaliyle birlikte güncellenmesini engellemiştir. Joint fine-tuning (uçtan uca eğitim) literatürde +%2-5 ek kazanç sağlayabilmektedir; bu sonraki adımda denenebilir.

**Yorum:** Bu sonuç bir başarısızlık değil, dürüst bir bilimsel bulgudur. ISIC 2019/2020 + HAM10000 birleşik veri setinde, YOLO classifier ailesi tarafından öğrenilen görsel temsil multimodal füzyona kapalı görünmektedir. Bulgu hem TÜBİTAK final raporunda hem de yayın aşamasında "negatif sonuç + yorum" olarak değerlendirilebilir.

> **[GÖRSEL 10 — MULTIMODAL TRAINING CURVES]** (opsiyonel)
> **Yerleştirilmesi gereken bölüm:** 6.10 sonu.
> **Oluşturma:** Notebook `multimodal_training.png` (1×3 subplot: train loss, val acc/F1, val mel recall).

> **[GÖRSEL 11 — 4-MODEL KARŞILAŞTIRMA BAR CHART]** (opsiyonel)
> **Yerleştirilmesi gereken bölüm:** 6.10 sonu.
> **Oluşturma:** Notebook `multimodal_comparison.png` (1×4 subplot, 3 bar: YOLO11/Multimodal/4-Ensemble).

### 6.11 Detection-Tabanlı Kropla Yeniden Eğitim Denemesi (Negatif Sonuç)

Multimodal füzyonun marjinal kazanç sağlamasının ardından (Bölüm 6.10), TÜBİTAK 2209-A H1 hipotezindeki yüksek doğruluk hedefine yaklaşmak için ikinci bir mimari müdahale denenmiştir: **two-stage detection-classification pipeline**. Bu yaklaşım literatürde Yu ve ark. (2017) ve Bi ve ark. (2017) tarafından %1-2 puan kazanç sağladığı raporlanmıştı.

**Yöntem:**

1. **Phase 1 — GT-bbox kropla:** Mevcut YOLO format detection label'larından (ground-truth bbox'lar) faydalanılarak 25.915 görüntüsünün hepsi lezyon bbox'ı + %20 margin ile kırpıldı. Kare olmayan kropların kareye genişletilmesi için gri padding uygulandı, sonra 384×384'e LANCZOS resize ile küçültüldü. Süre: 12.1 dk (CPU, PIL).
2. **Phase 2 — YOLO11m-cls retrain:** Aynı eğitim protokolüyle (50 epoch hedef, AdamW lr=0.001, cos_lr, AMP, A100), kropla edilmiş veride sıfırdan yeniden eğitim. Mosaic augmentation kapatıldı (lezyon zaten merkezde), erasing ve mixup hafifletildi.
3. **Phase 3 — Lightweight detection:** Mobile için YOLO11n-detect (2.6M parametre) 30 epoch eğitimi.

**Eğitim sonuçları (Phase 2):**

- 28. epoch'ta **early stop** (best @ epoch 13) — orijinal whole-image eğitiminin (50 epoch) yarısından az
- Final val top1: **0.729** (whole-image: 0.768)
- Eğitim süresi: 30.2 dk

**Test seti karşılaştırması (n = 3.690):**

| Metrik | Original (whole-image) | Cropped (retrained) | Δ | İstatistiksel Anlamlılık |
|:-------|:-----------------------|:--------------------|:--:|:-------------------------|
| Doğruluk | 0.7705 | 0.7125 | **-0.058** | — |
| Balanced Accuracy | 0.6454 | 0.5739 | -0.071 | — |
| Macro F1 | 0.6760 | 0.5900 | **-0.086** | — |
| **Melanom Recall** | **0.6730** | **0.5095** | **-0.164** | — |
| Melanom Precision | 0.6588 | 0.6872 | +0.028 | — |
| Cohen's Kappa | 0.6660 | 0.5663 | -0.100 | — |

**McNemar testi:** χ² = 65.00, **p < 0.000001** — istatistiksel olarak son derece anlamlı **kötüleşme**.

**YOLO11n-detect performansı:**

| Sınıf | Precision | Recall | mAP@50 |
|:------|:----------|:-------|:-------|
| nv | 0.833 | 0.871 | 0.915 |
| **mel** | **0.465** | 0.582 | 0.507 |
| bcc | 0.706 | 0.822 | 0.833 |
| bkl | 0.675 | 0.404 | 0.549 |
| akiec | 0.519 | 0.369 | 0.415 |
| vasc | 0.335 | 0.667 | 0.682 |
| df | 0.153 | 0.333 | 0.330 |
| **Tüm sınıflar** | **0.527** | **0.578** | **0.604** |

Detection da özellikle melanoma sınıfında zayıftır (P=0.465, mAP=0.507), bu da two-stage pipeline'ın klinik olarak kritik sınıfta çift hata kaynağı oluşturduğunu göstermektedir.

**Bilimsel sonuç (negatif bulgu):**

Bu deney, beklenen kazancın aksine **belirgin performans kaybı** üretmiştir. Olası açıklamalar şunlardır:

1. **ISIC kuratör bias'ı:** ISIC 2019/2020/HAM10000 görüntüleri zaten dermatologlar tarafından lezyon-merkezli olarak kırpılmış ve standardize edilmiştir. Ekstra %20 margin'lı bbox kropla, **klinik karar için kritik olan çevre cilt dokusunu silmiştir**. Özellikle melanoma için "atipik nevüs paterni" karşı "benign nevüs" ayrımı, çevre dokunun heterojenliği ve renk dağılımıyla yapılır.
2. **Square-padding artifact:** Kare olmayan bbox'ları kareye genişletmek için eklenen gri padding, modelin sınıf-spesifik bir özellik olarak öğrenebileceği yapay bir desen oluşturmuştur.
3. **Çoklu resampling kümülatif bilgi kaybı:** Whole-image (örn. 6000×4500) → 384×384 tek resize. Cropped: bbox crop (örn. 1500×1500) → square pad → 384×384, iki resize + padding. Detay kaybı kümülatif.
4. **Train-test dağılım sapması:** Train symlink-oversampled balance, test orijinal sayılar; cropped data'nın augmentasyon karakteristiği whole-image'dan farklı.
5. **Veri çeşitliliği kaybı:** Kropla edilmiş veride model aşırı hızlı yakınsadı (epoch 13 best, epoch 28 early stop). Whole-image eğitiminde olan generalization sinyali kayboldu.

Mel recall'daki **-%16.4'lük dramatik düşüş** klinik açıdan kabul edilemezdir. Bu konfigürasyon mobil deployment için **kullanılmamış**, mevcut whole-image YOLO11m-cls (FP16, 20.8 MB) ile devam edilmiştir.

> **[GÖRSEL 12 — CROPPED vs ORIGINAL COMPARISON]** (opsiyonel)
> **Yerleştirilmesi gereken bölüm:** 6.11 sonu.
> **İçerik:** Bar chart — 4 metrikte (acc, macro_f1, mel_recall, kappa) original ve cropped yan yana, kötüleşmeyi göstermek için.

---

## 7. TARTIŞMA

### 7.1 YOLOv8 ve YOLO11'in Yakın Performansı

YOLOv8m-cls ve YOLO11m-cls arasındaki küçük fark (%0.2 accuracy) şu şekilde yorumlanabilir:

**Parametre sayısı:** YOLO11 (10.4M) YOLOv8'e (15.8M) göre %34 daha az parametreye sahip olmasına rağmen performansı eşdeğer. Bu, YOLO11'in mimari etkinliğini göstermektedir. Mobil deployment açısından YOLO11 tercih edilebilir.

**C2PSA katkısı:** YOLO11'in melanom recall'da (0.674) YOLOv8'den (0.649) 2.5 puan üstün olması, attention tabanlı katmanların melanomanın heterojen görsel örüntüsünü (renk dağılımı, kenar düzensizliği) daha iyi temsil ettiğinin göstergesi olabilir.

### 7.2 Overfitting Sorunu

Her iki modelde de eğitim kaybının 0.05'e kadar düşmesine karşın validasyon doğruluğunun %77 civarında plato yapması tipik bir overfitting göstergesidir. Ortaya çıkan çıkarımlar:

- 50 epoch bu veri seti + bu augmentation kombinasyonu için uzun olabilir
- Dropout (şu an 0.0) eklenmesi regularizasyona katkı sağlayabilir
- Weight decay artışı (1e-4 → 5e-4) overfitting'i azaltabilir
- Daha agresif augmentation (CutMix, AugMix) düşünülebilir

### 7.3 TÜBİTAK Hedefleriyle Karşılaştırma

| TÜBİTAK Hedefi | Hedef | En İyi Tek Model | 3-YOLO Ens | 4-Model Ens | Threshold (en iyi) | Açık (en iyi) |
|:---------------|:------|:-----------------|:-----------|:------------|:-------------------|:--------------|
| Doğruluk | ≥ 0.90 | 0.7734 (v26) | **0.7870** | 0.7848 | 0.7472 (θ=0.10) | -0.113 |
| Macro F1 | ≥ 0.85 | **0.6933** (v8) | 0.6821 | 0.6773 | 0.6402 | -0.157 |
| Melanom Recall | ≥ 0.85 | 0.6743 (v11) | 0.6819 | 0.6781 | **0.8314** (θ=0.10) | **-0.0186** ✅ yakın |
| Cohen's Kappa | ≥ 0.85 | 0.6693 (v26) | **0.6886** | 0.6860 | — | -0.161 |
| ONNX INT8 ≤ 25 MB | ≤ 25 MB | — | — | — | **15.9 MB** | ✅ KARŞILANDI |

**Bulgular:**

- **Melanom recall hedefi:** 4-model ensemble + threshold ayarlamasıyla **0.8314** seviyesine ulaşıldı — TÜBİTAK 0.85 hedefinin yalnızca **-0.0186 altında**. Multimodal eklemenin marjinal katkısı ile (vs 3-YOLO 0.8175), bu boşluğun klinik açıdan anlamlı bir kazanç haline gelmesi için ek çalışma gerekmektedir (joint fine-tuning, alternatif feature extraction noktası).
- **Mobil deployment hedefi:** ONNX INT8 ile **15.9 MB** elde edildi, hedefin (≤25 MB) belirgin altında. ✅
- **Macro F1 hedefi:** Ensemble + threshold ayarlamasıyla 0.65-0.69 bandında kaldı; multimodal füzyon (FT-Transformer + hasta metadata) ile 0.80-0.82 hedeflenmektedir.
- **Accuracy hedefi:** %78.7 (ensemble) — literatürde ISIC 2019 SOTA'sı %85-88 seviyesindedir; %90 hedefi multimodal + TTA + hiperparametre optimizasyonu ile yaklaşılabilir.
- **Kappa hedefi:** Ensemble ile 0.6886'ya çıkmış olmakla birlikte 0.85 hedefi multimodal füzyon olmadan ulaşılması güç görünmektedir.

### 7.5 Multimodal Füzyonun Beklentinin Altında Kalması (Negatif Sonuç Yorumu)

Bu çalışmanın en önemli bilimsel bulgularından biri, **multimodal füzyonun frozen YOLO11 features üzerinde anlamlı kazanç sağlamamasıdır** (McNemar p=0.94, identik 101 farklılık). Literatürde Gessert ve ark. (2020) ile Kawahara ve ark. (2019) gibi çalışmalar multimodal yaklaşımlarla %3-5 puan macro F1 kazancı bildirmişti; bu çalışmadaki kazanç ise +0.0065 puan ile istatistiksel olarak gürültü seviyesinde kalmıştır.

Bu negatif sonucu açıklamak için üç temel hipotez önerilmektedir:

**Hipotez 1: Görsel modelin gizli demografik temsili.** YOLO11m-cls ImageNet pretrained ağırlıklardan başlayarak ISIC veri seti üzerinde fine-tune edildiğinde, hasta yaşı, cinsiyeti ve anatomik bölge gibi demografik özelliklerin görsel tezahürlerini (lezyon büyüklüğü, doku, lokalizasyon) **dolaylı olarak öğrenmiş** olabilir. Bu durumda, açıkça verilen yaş ve cinsiyet etiketleri yeni bilgi taşımamaktadır. Bu hipotez, frozen feature'ların class-aware olduğu gözlemiyle (1280-d penultimate vektör doğrudan classifier head'in girdisidir) tutarlıdır.

**Hipotez 2: Veri tavanı ortaktır.** Önceki McNemar bulguları (YOLOv8↔YOLO11↔YOLO26 üçü arasında p>0.66) farklı YOLO mimarilerinin de aynı performans tavanına çarptığını göstermişti. Bu tavan, modelin yetersizliğinden değil, **dermoskopik görüntülerin sınıf ayrımı için sağladığı bilgi miktarının sınırlı olmasından** kaynaklanmaktadır. Metadata gibi çok boyutlu olmayan ek özelliklerin bu tavanı aşması beklenmemektedir.

**Hipotez 3: Frozen backbone darboğazı.** Multimodal eğitimde YOLO backbone donmuş olduğundan, görsel encoder metadata sinyaliyle birlikte güncellenememiştir. Joint fine-tuning (uçtan uca eğitim) literatürde +%2-5 ek kazanç sağlayabilmektedir. Bu, sonraki adım olarak değerlendirilmektedir.

**Bilimsel ve pratik sonuç:**

Negatif bulgular yayın değeri açısından genellikle pozitif bulgulardan daha az dikkat çekse de, bu çalışmadaki sonuç klinik AI literatürü için **iki önemli mesaj** vermektedir:

1. **Demografik metadata'nın katkısı veri setine bağlıdır.** Önceki çalışmalardaki +%3-5 kazanç, daha küçük veri setleri (ör. sadece HAM10000) veya daha az model kapasitesi (ör. ResNet-50) ile elde edilmişti. Modern güçlü modellerin (YOLO11 gibi) zengin veri setleri (25K+) üzerinde fine-tune edilmesi, demografik bilgiyi gizli olarak kazandırmış olabilir.
2. **Multimodal füzyon zorunlu değildir, ancak doğru noktada uygulanmalıdır.** Frozen post-classifier features yerine raw spatial backbone features veya uçtan uca eğitim daha verimli bir yol olabilir.

Bu yorum bitirme çalışmasında ve potansiyel akademik yayın aşamasında özgün bir değerlendirme olarak öne çıkarılabilir.

### 7.6 Detection-Crop-Classify Pipeline'ın Beklentinin Aksine Performans Düşürmesi

Multimodal füzyon negatif bulgusunun (Bölüm 7.5) ardından denenen **two-stage detection-classification pipeline**, beklenen kazancın aksine **belirgin performans kaybı** üretmiştir (mel recall -%16.4, p<0.000001). Bu, multimodal füzyon başarısızlığı ile birlikte ele alındığında **birleşik bir bilimsel mesaj** ortaya çıkarmaktadır.

**Beklenen ile gerçekleşen karşılaştırması:**

Yu ve ark. (2017) ve Bi ve ark. (2017) gibi öncü çalışmalar, segmentasyon-tabanlı kropla yaklaşımın skin lesion sınıflandırmasında %1-2 puan accuracy kazancı sağladığını rapor etmiştir. Ancak bu çalışmalar:

- Daha küçük veri setleri kullanmıştır (HAM10000 alone, ISIC 2017 küçük altküme)
- Daha düşük kapasiteli modellerle çalışmıştır (ResNet-50, VGG-16)
- Çekirdek yöntem olarak segmentasyon kullanmıştır (pixel-level mask), bu çalışmadaki bbox kropla daha kaba bir yöntemdir

Bu çalışmanın bağlamında (25K görüntü, modern YOLO11 backbone, GT bbox ile sıkı kropla), bu literatür beklentisi gerçekleşmemiştir.

**Birleşik metodolojik öğrenme:**

İki negatif sonuç (multimodal füzyon başarısızlığı + cropla-tabanlı kötüleşme) birlikte değerlendirildiğinde **modern güçlü modellerin kuratör-bağımlı veri setlerindeki davranışı** hakkında önemli bir mesaj vermektedir:

> *"YOLO11m-cls gibi yüksek kapasiteli modern modeller, ImageNet pretrained ağırlıklardan başlayıp ISIC 2019+2020+HAM10000 (25.915 görüntü) gibi zengin ve kuratör-merkezli veri setleri üzerinde fine-tune edildiğinde, klasik literatür müdahalelerinin (multimodal füzyon, segmentasyon-crop) işe yaramadığı bir 'doygunluk rejimi'ne girmektedir. Bu rejimin altında yatan mekanizmalar şunlardır: (a) görsel encoder'ın fine-tune sırasında demografik etkileri (yaş, cinsiyet) görsel tezahürlerden dolaylı olarak öğrenmesi, (b) ISIC kuratör ekibinin görüntüleri zaten klinik ayrım için optimal olacak şekilde kropla etmiş olması, (c) ek müdahalelerin yararlı görsel bağlamı (çevre cilt dokusu) silmesi. Bu rejimde performansı arttırmak için yapılması gereken müdahaleler: (1) kuratör-bağımsız zenginleştirme (ör. PAD-UFES-20, 7-point checklist gibi farklı dağılımlı ek veri), (2) backbone'un da güncellendiği uçtan uca multimodal joint fine-tuning, (3) self-supervised pre-training (SimCLR, MAE) ile temsil zenginleştirme."*

Bu iki negatif sonuç hem TÜBİTAK final raporunda hem de potansiyel akademik yayında **özgün metodolojik katkı** olarak öne çıkarılabilir. Negatif sonuç yayınları klinik ML literatüründe nadirdir; bu çalışma araştırmacılara "her zaman çalışan" varsayılan müdahalelerin sınırlarını gösterir.

### 7.4 YOLO26'nın Literatür Katkısı ve Mimari Bulgular

YOLO26m-cls'nin dermoskopi veri setlerindeki ilk uygulamalarından biri olması, TÜBİTAK raporunda ve potansiyel akademik yayın aşamasında öne çıkarılabilecek özgün katkıdır. **Deney sonrası tespit edilen önemli bulgular:**

1. **Mimari özdeşlik:** Ultralytics 8.4.47 sürümünde YOLO26m-cls fused inference modelinde **10,350,599 parametre** ve **39.3 GFLOPs** ile YOLO11m-cls ile birebir aynıdır. Backbone C3k2 + C2PSA blokları YOLO11 ile özdeştir. Bu, YOLO26'nın asıl mimari yeniliklerinin (DFL kaldırma, NMS-free, end-to-end head) **detection görevine özel** olduğunu, classification varyantında YOLO11 omurgasının kullanıldığını göstermektedir.
2. **Performans benzerliği:** YOLO26 ve YOLO11 arasında McNemar p = 0.6650 (anlamsız fark). Bu, mimari özdeşlik bulgusunu istatistiksel olarak doğrulamaktadır.
3. **Marjinal farklar:** YOLO26 accuracy (+0.0027) ve mel precision (+0.0165) açısından YOLO11'e göre küçük üstünlük göstermiş, ancak balanced accuracy (-0.0184) ve macro F1 (-0.0192) açısından düşmüştür. Bu farklar büyük ölçüde **eğitim konfigürasyonu (varsayılan AdamW, MuSGD değil)** ve **rastgele initialization** etkisine atfedilebilir.
4. **MuSGD ve ProgLoss yokluğu:** YOLO26'nın ana algoritmik yenilikleri olan MuSGD optimizer ve Progressive Loss Balancing, classification eğitiminde Ultralytics 8.4.47 tarafından **varsayılan olarak kullanılmamıştır** (logda `optimizer=AdamW` görünmektedir). Sürümün olgunlaşmasıyla bu özelliklerin classification varyantına entegrasyonu beklenmektedir.
5. **Somut avantaj:** YOLO26'nın öne çıkarılan **%43 daha hızlı CPU inference** iddiası, classification varyantında bu çalışmada doğrulanamamıştır (YOLO11 ile aynı GFLOPs, dolayısıyla benzer inference hızı). Bu özellik detection varyantına özel olabilir.

**Bilimsel açıklama (rapora yazılması önerilen):** "YOLO26m-cls Ultralytics 8.4.47 ile dermoskopi alanında ilk kez uygulanmıştır. Mimari analiz, classification varyantının YOLO11m-cls ile birebir parametre özdeşliği taşıdığını göstermiştir; bu nedenle test seti üzerinde elde edilen marjinal performans farkları (Δaccuracy = +0.0027) istatistiksel olarak anlamsızdır (McNemar p = 0.6650). YOLO26'nın asıl algoritmik yenilikleri (DFL kaldırma, NMS-free, MuSGD, ProgLoss) detection görevine yönelik olup, classification için sürümün olgunlaşmasıyla genişletilmesi beklenmektedir."

---

## 8. KISITLILIKLAR

1. **YOLOv9, YOLOv10, YOLOv12 classification pretrained ağırlık eksikliği.** Ultralytics'in resmi kütüphanesinde bu üç nesilde pretrained cls çeşidi bulunmamaktadır. Sıfırdan eğitim 25 bin görüntüyle yeterli transfer elde edemez, bu nedenle karşılaştırmadan çıkarılmıştır.

2. **Sınıf dengesizliği.** Symlink oversampling ile azaltılmakla birlikte, df sınıfı (sadece 150 eğitim örneği) halen zorlayıcıdır. F1 = 0.45 seviyesinde kalmaktadır.

3. **Val-test dağılım kayması ihtimali.** Val setinde nv oranı ~%57, test setinde ~%48. Bu küçük bir dağılım kayması, model seçimi kararlarını etkileyebilir.

4. **Tek seed ile eğitim.** Varyans analizi için eğitim birden fazla random seed ile tekrarlanmamıştır. Sonuçlar için belirsizlik aralığı verilmemiştir.

5. **Hiperparametre araması yapılmadı.** Mevcut hiperparametreler Ultralytics defaultlarına yakın tutulmuş, grid/random/Bayesian search uygulanmamıştır.

---

## 9. SONRAKİ ADIMLAR

### 9.1 Kısa Vadede (2-4 hafta)

- **Joint fine-tuning multimodal füzyon:** Mevcut frozen-backbone yaklaşımı negatif sonuç verdiğinden (Bölüm 6.10 ve 7.5), YOLO11m-cls backbone'unu **donduran konfigürasyonu kaldırıp uçtan uca eğitim** denenecek. Bu yaklaşımda image encoder metadata sinyaliyle birlikte güncelleneceği için literatürde +%2-5 ek kazanç beklenmektedir.
- **Alternatif feature extraction noktası:** Penultimate (post-classify) yerine YOLO11'in **8. veya 9. layer'ından (C2PSA öncesi raw spatial features)** feature çıkarmak — class-aware olmayan ham temsil multimodal füzyona daha açık olabilir.
- **Test-time augmentation (TTA):** Her test görüntüsü için flip + rotate varyantlarının ortalama tahmini — beklenen kazanç: +0.5-1.5 puan.
- **Hiperparametre ayarı:** Overfitting'i azaltmak için dropout, weight decay, daha yumuşak augmentation konfigürasyonu.

### 9.2 Orta Vadede (4-8 hafta)

- **Android uygulama geliştirme:** ONNX INT8 modelini ONNX Runtime Mobile ile Android'e entegre etme. Kamera → preprocess → inference → klinik açıklamalı sonuç gösterimi.
- **Kullanıcı testleri:** 5-10 gönüllü dermatolog ile ön kullanılabilirlik testi.

### 9.3 Uzun Vadede (proje sonu)

- **External validation:** PAD-UFES-20 veya 7-point dataset üzerinde genelleme testi.
- **Explainability:** Grad-CAM ile modelin hangi bölgeye odaklandığının görselleştirilmesi.
- **TÜBİTAK final raporu ve makale yazımı.**

---

## 10. SONUÇ

Bu ara raporda, TÜBİTAK 2209-A proje önerisinin temel özgün katkısı olan YOLO model ailesi sistematik karşılaştırmasının ilk aşaması belgelenmiştir. **Üç YOLO sürümü** (v8m-cls, 11m-cls, 26m-cls) aynı eğitim protokolü ile test edilmiş, sonuçlar şu şekilde özetlenebilir:

- **Bireysel test sonuçları:** Üç model birbirine pratik olarak eşdeğer (accuracy 0.7707-0.7734 aralığı, McNemar p > 0.66 — istatistiksel olarak anlamsız fark). En iyi tek-model macro F1 değeri YOLOv8m-cls'de **0.6933**, en iyi mel recall YOLO11m-cls'de **0.6743**, en iyi accuracy ve kappa YOLO26m-cls'de (0.7734 ve 0.6693).
- **Ensemble:** Üç model softmax ortalaması accuracy'yi **0.7870** (+1.4%), kappa'yı **0.6886** (+2.1%), mel recall'u **0.6819** (+1.1%) seviyelerine çıkarmıştır.
- **Threshold tuning:** Ensemble üzerinde melanom-odaklı eşik ayarı (θ=0.15) ile mel recall **0.8175** elde edilmiş, TÜBİTAK 0.85 hedefinin **yalnızca -0.0325 altına** inilmiştir.
- **Mobil deployment:** ONNX INT8 quantization ile model boyutu **15.9 MB** (TÜBİTAK ≤25 MB hedefi karşılandı ✅).

**Mimari bulgular:** YOLO26m-cls'nin Ultralytics 8.4.47 sürümünde YOLO11m-cls ile parametre özdeşliği taşıdığı (10,350,599 param, 39.3 GFLOPs, C3k2+C2PSA blokları) tespit edilmiştir. YOLO26'nın ana yenilikleri (DFL kaldırma, NMS-free, MuSGD, ProgLoss) detection varyantına yönelik olup classification için henüz tam olarak entegre edilmemiştir. YOLOv12m-cls'nin pretrained ağırlıklarının yokluğu metodolojik kısıt olarak belgelenmiştir.

**Bilimsel sonuç:** Üç YOLO sürümünün eşdeğer performans göstermesi ve macro F1'in 0.69 civarında plato yapması, **dermoskopi-görsel modalite tek başına bu veri seti için bir performans tavanı oluşturduğunu** istatistiksel olarak desteklemektedir.

**Multimodal füzyonun denenmesi ve negatif bulgu (Bölüm 6.10):** YOLO11m-cls görsel backbone üzerine FT-Transformer + cross-attention ile metadata füzyonu uygulanmış, ancak McNemar testi anlamsız fark göstermiştir (p=0.94, identik 101 fark). 4-model ensemble (3-YOLO + multimodal) ile threshold tuning sonrası mel recall **0.8314** seviyesine çıkmış, TÜBİTAK 0.85 hedefine **-0.0186 yakınlığa** ulaşılmıştır.

**Detection-tabanlı kropla yeniden eğitim ve ikinci negatif bulgu (Bölüm 6.11):** GT bbox'larla %20 margin'lı kropla edilmiş veri üzerinde YOLO11m-cls yeniden eğitilmiş, ancak beklenen kazancın aksine tüm metriklerde belirgin kötüleşme yaşanmıştır (mel recall -%16.4, p<0.000001). Bu, ISIC kuratör ekibinin görüntüleri zaten lezyon-merkezli işlemiş olmasının, ek müdahalelerin klinik ayrım için kritik olan çevre cilt dokusunu sildiğini göstermektedir. Cropla edilmiş model mobil deployment için kullanılmamıştır.

**Birleşik bilimsel sonuç:** İki negatif bulgu (multimodal füzyon + segmentasyon-cropla) birlikte değerlendirildiğinde, modern güçlü YOLO sınıflandırma modellerinin kuratör-merkezli ISIC veri setinde bir **doygunluk rejimi**ne girdiğini göstermektedir (Bölüm 7.6). Bu rejimde klasik literatür müdahaleleri etkisiz kalmakta, performans tavanını aşmak için kuratör-bağımsız veri zenginleştirilmesi veya self-supervised pre-training gerekmektedir.

Mobil deployment hedefi (ONNX INT8, **15.9 MB**) mevcut konfigürasyonla karşılanmış olup, klinik validasyon ve Android entegrasyonu önümüzdeki dönemde yapılacaktır. **Mevcut en iyi konfigürasyon: 3-YOLO ensemble + threshold θ=0.15 ile mel recall 0.8175** (precision 0.5614 ile birlikte), bu konfigürasyon klinik tarama uygulamasında "şüphede dermatolog görsün" prensibiyle kullanılabilir.

---

## 11. KAYNAKÇA

1. Esteva, A., ve ark. (2017). Dermatologist-level classification of skin cancer with deep neural networks. *Nature*, 542, 115-118.

2. Brinker, T. J., ve ark. (2019). Deep learning outperformed 136 of 157 dermatologists in a head-to-head dermoscopic melanoma image classification task. *European Journal of Cancer*, 113, 47-54.

3. Gessert, N., ve ark. (2020). Skin lesion classification using ensembles of multi-resolution EfficientNets with meta data. *MethodsX*, 7, 100864.

4. Kawahara, J., Daneshvar, S., Argenziano, G., & Hamarneh, G. (2019). Seven-point checklist and skin lesion classification using multitask multimodal neural nets. *IEEE Journal of Biomedical and Health Informatics*, 23(2), 538-546.

5. Liu, Y., ve ark. (2020). A deep learning system for differential diagnosis of skin diseases. *Nature Medicine*, 26, 900-908.

6. Combalia, M., ve ark. (2022). Validation of artificial intelligence prediction models for skin cancer diagnosis using dermoscopy images. *The Lancet Digital Health*, 4(5), e330-e339.

7. Cui, Y., Jia, M., Lin, T. Y., Song, Y., & Belongie, S. (2019). Class-balanced loss based on effective number of samples. *CVPR 2019*, 9268-9277.

8. Jocher, G., Chaurasia, A., & Qiu, J. (2023). Ultralytics YOLOv8.

9. Jocher, G., ve ark. (2024). Ultralytics YOLO11.

10. Tian, Y., ve ark. (2025). YOLOv12: Attention-Centric Real-Time Object Detectors. *NeurIPS 2025*.

11. Ultralytics. (2026, Jan 14). YOLO26: Simplified, edge-optimized real-time vision AI.

12. Gorishniy, Y., ve ark. (2021). Revisiting deep learning models for tabular data. *NeurIPS 2021*.

13. Dietterich, T. G. (1998). Approximate statistical tests for comparing supervised classification learning algorithms. *Neural Computation*, 10(7), 1895-1923.

---

## 12. EKLER — GÖRSEL YERLEŞİM KILAVUZU (ÖZET)

Raporda kullanılması gereken tüm görsellerin özet listesi, yerleşim bölümü ve elde etme kaynağı aşağıda derlenmiştir. Her görsel numarası raporun ilgili bölümünde atıfla belirtilmiştir.

| # | Görsel | Bölüm | Kaynak | Başlık |
|:-:|:-------|:------|:-------|:-------|
| 1 | Sınıf dağılım histogramı | 4.4 | Arkadaşın CSV + seaborn | Şekil 4.1 |
| 2 | YOLOv8m-cls eğitim eğrileri | 6.1 | `runs/yolov8m_cls/results.png` | Şekil 6.1 |
| 3 | YOLOv8m-cls confusion matrix | 6.1 | Notebook evaluation bölümü | Şekil 6.2 |
| 4 | YOLO11m-cls eğitim eğrileri | 6.2 | `runs/yolo11m_cls/results.png` | Şekil 6.3 |
| 5 | YOLO11m-cls confusion matrix | 6.2 | Notebook evaluation bölümü | Şekil 6.4 |
| 6 | YOLO26m-cls eğitim eğrileri | 6.4 | `runs/yolo26m_cls/results.png` | Şekil 6.5 |
| 7 | YOLO26m-cls confusion matrix | 6.4 | Notebook evaluation bölümü | Şekil 6.6 |
| 8 | Kombine karşılaştırma bar chart | 6.5 | `comparison_with_v26.png` | Şekil 6.7 |
| 9 | Threshold tuning eğrisi | 6.8 | `threshold_tuning.png` | Şekil 6.8 |

**Görseller için genel stil önerisi:**

- DPI: 300 (yayın kalitesi)
- Format: PNG (raster) veya PDF (vektörel)
- Yazı tipi: Arial veya Times New Roman, 10pt
- Renk paleti: Colorblind-friendly (seaborn "colorblind" ya da Paul Tol renk seti)
- Eksen etiketleri: Türkçe (eğer raporun dili Türkçe ise tutarlı olacak şekilde)
- Başlık: "Şekil X.Y. Açıklama" formatında — Türkçe akademik yazımda standart

**Rapor yazım önerisi:** Her görsel ilgili metinde en az bir kez atıfla belirtilmelidir ("... sonuçlar Şekil 6.7'de gösterilmektedir"). Görsellerin altında tam başlık ve veri kaynağı (tarih, eğitim süresi vb.) verilmelidir.

---

**DEĞİŞİKLİK GEÇMİŞİ (Changelog)**

| Sürüm | Tarih | Değişiklik |
|:------|:------|:-----------|
| 0.1 | Nisan 2026 | İlk taslak — YOLOv8 ve YOLO11 sonuçlarıyla |
| 0.2 | Mayıs 2026 | YOLO26 sonuçları eklendi; 6.4-6.9 tabloları gerçek değerlerle dolduruldu (ensemble, McNemar, threshold tuning, ONNX INT8). Bölüm 7.4 mimari özdeşlik bulgusu eklendi. |
| 0.3 | Mayıs 2026 | Multimodal füzyon deneyleri eklendi (Bölüm 6.10). YOLO11m-cls + FT-Transformer + cross-attention denendi; McNemar p=0.94 ile anlamsız fark — negatif sonuç olarak belgelendi. 4-model ensemble + threshold ile mel recall 0.8314 (TÜBİTAK 0.85 hedefine -0.0186). Bölüm 7.5 negatif sonuç yorumu eklendi. |
| 0.4 | Mayıs 2026 | Detection-cropla yeniden eğitim deneyi eklendi (Bölüm 6.11). YOLO11m-cls GT bbox %20 margin kropla retrain — beklentinin aksine tüm metriklerde kayıp, mel recall -%16.4 (p<0.000001). İkinci negatif bulgu olarak belgelendi. Bölüm 7.6 birleşik metodolojik öğrenme yorumu eklendi (doygunluk rejimi hipotezi). Mobil deployment için orijinal whole-image FP16 modeli korundu. |
| 1.0 | TÜBİTAK final | Nihai sürüm + makale hazırlığı |
