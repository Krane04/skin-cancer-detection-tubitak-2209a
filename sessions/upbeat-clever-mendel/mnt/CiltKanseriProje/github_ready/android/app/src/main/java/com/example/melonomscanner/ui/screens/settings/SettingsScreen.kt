package com.example.melonomscanner.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.melonomscanner.ui.components.SectionHeader
import com.example.melonomscanner.ui.theme.ThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle(initialValue = ThemeOption.SYSTEM)
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle(initialValue = true)
    val biometric by viewModel.biometricLock.collectAsStateWithLifecycle(initialValue = false)
    val haptics by viewModel.haptics.collectAsStateWithLifecycle(initialValue = true)

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("Görünüm")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOption.entries.forEach { option ->
                    FilterChip(
                        selected = theme == option,
                        onClick = { viewModel.setTheme(option) },
                        label = {
                            Text(
                                when (option) {
                                    ThemeOption.SYSTEM -> "Sistem"
                                    ThemeOption.LIGHT -> "Açık"
                                    ThemeOption.DARK -> "Koyu"
                                }
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SettingRow(
                title = "Dinamik Renk (Material You)",
                subtitle = "Duvar kağıdı renklerini uygulayın (Android 12+)",
                checked = dynamicColor,
                onCheckedChange = { viewModel.setDynamicColor(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader("Güvenlik")
            Spacer(modifier = Modifier.height(8.dp))
            SettingRow(
                title = "Biyometrik Kilit",
                subtitle = "Uygulamayı parmak izi/yüz ile kilitle",
                checked = biometric,
                onCheckedChange = { viewModel.setBiometric(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader("Erişilebilirlik")
            Spacer(modifier = Modifier.height(8.dp))
            SettingRow(
                title = "Dokunsal Geri Bildirim",
                subtitle = "Aksiyonlarda titreşim",
                checked = haptics,
                onCheckedChange = { viewModel.setHaptics(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Veri")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Tüm Tarama Verilerini Sil")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "KVKK kapsamında istediğiniz zaman tüm verileriniz silinebilir. Bu işlem geri alınamaz.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "MelonomScanner v1.0 — TÜBİTAK 2209-A",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Tüm tarama verileri silinsin mi?") },
            text = {
                Text("Bu işlem geri alınamaz. Tüm fotoğraflar ve geçmiş kayıtlarınız silinecek.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData()
                    showDeleteDialog = false
                }) { Text("Evet, Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
