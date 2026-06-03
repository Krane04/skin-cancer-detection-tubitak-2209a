package com.example.melonomscanner.data.model

import androidx.compose.ui.graphics.Color
import com.example.melonomscanner.ui.theme.RiskCritical
import com.example.melonomscanner.ui.theme.RiskHigh
import com.example.melonomscanner.ui.theme.RiskLow
import com.example.melonomscanner.ui.theme.RiskMedium

/** Kullanıcı cinsiyeti (HAM10000 standardına uyumlu) */
enum class Sex(val label: String, val onnxValue: Int) {
    MALE("Erkek", 0),
    FEMALE("Kadın", 1),
    UNKNOWN("Belirtmek istemiyorum", -1)
}

/** Fitzpatrick cilt tipi (I–VI) */
enum class FitzpatrickType(val level: Int, val title: String, val desc: String, val swatch: Long) {
    TYPE_I(1, "Tip I", "Çok açık — daima yanar, hiç bronzlaşmaz", 0xFFF8DCC0),
    TYPE_II(2, "Tip II", "Açık — genelde yanar, az bronzlaşır", 0xFFF1C9A5),
    TYPE_III(3, "Tip III", "Orta — bazen yanar, kademeli bronzlaşır", 0xFFD8A778),
    TYPE_IV(4, "Tip IV", "Zeytin — nadiren yanar, kolay bronzlaşır", 0xFFA47551),
    TYPE_V(5, "Tip V", "Esmer — çok nadir yanar", 0xFF6F4E37),
    TYPE_VI(6, "Tip VI", "Koyu — asla yanmaz", 0xFF3B2617)
}

/** Anatomik bölgeler (FT-Transformer metadata için tek-sıcak kodlamada kullanılır) */
enum class BodyRegion(val label: String, val code: String) {
    HEAD("Baş / Yüz", "head"),
    NECK("Boyun", "neck"),
    CHEST("Göğüs", "chest"),
    ABDOMEN("Karın", "abdomen"),
    BACK_UPPER("Sırt (Üst)", "back_upper"),
    BACK_LOWER("Sırt (Alt)", "back_lower"),
    ARM_UPPER("Üst Kol", "arm_upper"),
    ARM_LOWER("Ön Kol", "arm_lower"),
    HAND("El", "hand"),
    LEG_UPPER("Uyluk", "leg_upper"),
    LEG_LOWER("Baldır", "leg_lower"),
    FOOT("Ayak", "foot"),
    SCALP("Saçlı Deri", "scalp"),
    GENITAL("Genital / Perine", "genital")
}

/** Tarama sırasında FT-Transformer'a gidecek hasta metadatasi */
data class PatientMetadata(
    val age: Int,
    val sex: Sex,
    val fitzpatrick: FitzpatrickType,
    val region: BodyRegion,
    val lesionSizeMm: Float? = null,
    val familyHistory: Boolean = false
)

/** Risk seviyeleri — modelin bulgu sınıfına ve güvenine göre atanır */
enum class RiskLevel(val label: String, val color: Color, val recommendation: String) {
    LOW("Düşük", RiskLow,
        "Bu lezyon iyi huylu görünmektedir. 6 ayda bir rutin kontrol önerilir."),
    MEDIUM("Orta", RiskMedium,
        "Bulgu belirsiz. Önümüzdeki 1-2 ay içinde dermatoloğa danışmanız önerilir."),
    HIGH("Yüksek", RiskHigh,
        "Yüksek riskli bulgular var. Önümüzdeki hafta içinde dermatoloğa başvurun."),
    CRITICAL("Kritik", RiskCritical,
        "Kritik bulgular. 48 saat içinde bir dermatoloğa başvurun.")
}
