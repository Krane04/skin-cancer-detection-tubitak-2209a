package com.example.melonomscanner.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.melonomscanner.ui.components.ActionCard
import com.example.melonomscanner.ui.components.Divider16
import com.example.melonomscanner.ui.components.SectionHeader
import com.example.melonomscanner.ui.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewScan: () -> Unit,
    onHistory: () -> Unit,
    onInfo: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val totalScans by viewModel.totalScans.collectAsStateWithLifecycle(initialValue = 0)
    val followed by viewModel.followedLesions.collectAsStateWithLifecycle(initialValue = 0)
    val flaggedCount by viewModel.flaggedCount.collectAsStateWithLifecycle(initialValue = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MelonomScanner",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Üst özet kart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Merhaba!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cildinizdeki değişimleri takip edin, düzenli kontrol alışkanlığı edinin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Divider16()

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    value = totalScans.toString(),
                    label = "Toplam Tarama",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = followed.toString(),
                    label = "Takipli Lezyon",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = flaggedCount.toString(),
                    label = "İşaretli",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider16()
            SectionHeader(title = "Hızlı İşlemler")
            Spacer(modifier = Modifier.height(12.dp))

            ActionCard(
                title = "Yeni Tarama",
                subtitle = "Hasta bilgisi + kamerayla analiz",
                icon = Icons.Filled.Camera,
                onClick = onNewScan
            )
            Spacer(modifier = Modifier.height(10.dp))
            ActionCard(
                title = "Tarama Geçmişi",
                subtitle = "Önceki lezyon kayıtları",
                icon = Icons.Filled.History,
                tint = MaterialTheme.colorScheme.tertiary,
                onClick = onHistory
            )
            Spacer(modifier = Modifier.height(10.dp))
            ActionCard(
                title = "ABCDE Kuralı",
                subtitle = "Cilt lezyonlarını kendiniz değerlendirin",
                icon = Icons.Filled.Info,
                tint = MaterialTheme.colorScheme.secondary,
                onClick = onInfo
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
