package com.example.melonomscanner.ui.screens.result

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.melonomscanner.data.model.LesionClass
import com.example.melonomscanner.data.model.RiskLevel
import com.example.melonomscanner.ui.components.HorizontalPill
import com.example.melonomscanner.ui.components.RiskIndicator
import com.example.melonomscanner.ui.components.SectionHeader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    scanId: Long,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: ResultViewModel = viewModel()
) {
    LaunchedEffect(scanId) { viewModel.load(scanId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarama Sonucu", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.History, contentDescription = "Geçmiş")
                    }
                }
            )
        }
    ) { inner ->
        val scan = state.scan
        if (scan == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center
            ) { Text("Yükleniyor...") }
            return@Scaffold
        }

        val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr", "TR")) }
        val dateText = dateFormatter.format(Date(scan.timestamp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val bitmap = remember(scan.imagePath) {
                runCatching {
                    val f = File(scan.imagePath)
                    if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                }.getOrNull()
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val risk = runCatching { RiskLevel.valueOf(scan.riskLevel) }.getOrDefault(RiskLevel.LOW)
            RiskIndicator(risk = risk, confidence = scan.confidence)

            Spacer(modifier = Modifier.height(16.dp))

            // === Ana sonuç kartı ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = scan.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    val primary = state.primary
                    if (primary != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = primary.shortName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalPill(
                            label = scan.classCode.uppercase(Locale.US),
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalPill(
                            label = "%${(scan.confidence * 100).toInt()} güven",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (state.primary?.isMalignant == true) {
                            HorizontalPill(
                                label = "Kötü Huylu",
                                color = Color(0xFFDC2626)
                            )
                        } else if (state.primary != null) {
                            HorizontalPill(
                                label = "İyi Huylu",
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Yaş: ${scan.age}  •  Cinsiyet: ${scan.sex}  •  Bölge: ${scan.region}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tarama Zamanı: $dateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // === Sonuç açıklaması (sınıf-özelinde, kullanıcı dostu) ===
            val primary = state.primary
            if (primary != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ExplanationCard(
                    icon = Icons.Filled.Info,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Bu Ne Anlama Geliyor?",
                    body = primary.userExplanation,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f),
                )

                Spacer(modifier = Modifier.height(12.dp))
                ExplanationCard(
                    icon = Icons.Filled.RemoveRedEye,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Tipik Görünüm",
                    body = primary.typicalAppearance,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.30f),
                )

                Spacer(modifier = Modifier.height(12.dp))
                // Eylem kartı — risk seviyesine göre renk-kodlu
                val actionBg = when (risk) {
                    RiskLevel.LOW -> Color(0xFFD1FAE5)
                    RiskLevel.MEDIUM -> Color(0xFFFEF3C7)
                    RiskLevel.HIGH -> Color(0xFFFED7AA)
                    RiskLevel.CRITICAL -> Color(0xFFFEE2E2)
                }
                val actionIconTint = when (risk) {
                    RiskLevel.LOW -> Color(0xFF16A34A)
                    RiskLevel.MEDIUM -> Color(0xFFF59E0B)
                    RiskLevel.HIGH -> Color(0xFFEA580C)
                    RiskLevel.CRITICAL -> Color(0xFFDC2626)
                }
                ExplanationCard(
                    icon = Icons.Filled.LocalHospital,
                    iconTint = actionIconTint,
                    title = "Ne Yapmalısınız?",
                    body = primary.clinicalAction,
                    backgroundColor = actionBg,
                    emphasized = risk == RiskLevel.HIGH || risk == RiskLevel.CRITICAL,
                )
            }

            // === Olasılık dağılımı (mevcut) ===
            if (state.topK.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Olasılık Dağılımı")
                Spacer(modifier = Modifier.height(8.dp))
                state.topK.forEach { (label, prob) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "%${(prob * 100).toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { prob.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // === Tıbbi uyarı (alt sabit kart) ===
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text(
                            text = "Tıbbi Uyarı",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Bu uygulama tıbbi teşhis aracı değildir; sonuçlar ön " +
                                "değerlendirme niteliğindedir. Şüpheli her lezyon için " +
                                "mutlaka bir dermatoloğa başvurun.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Sınıf açıklamaları için yeniden kullanılabilir kart. */
@Composable
private fun ExplanationCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String,
    backgroundColor: Color,
    emphasized: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (emphasized) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
