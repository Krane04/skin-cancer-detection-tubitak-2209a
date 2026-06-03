package com.example.melonomscanner.ui.screens.metadata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.melonomscanner.data.model.Sex
import com.example.melonomscanner.ui.components.BodyMap
import com.example.melonomscanner.ui.components.Divider16
import com.example.melonomscanner.ui.components.FitzpatrickSelector
import com.example.melonomscanner.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataScreen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: MetadataViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasta Bilgileri", fontWeight = FontWeight.SemiBold) },
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
            SectionHeader(
                title = "Temel Bilgiler",
                subtitle = "Bu bilgiler FT-Transformer modeline metadata olarak verilir."
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.ageText,
                onValueChange = { viewModel.setAge(it) },
                label = { Text("Yaş") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cinsiyet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sex.entries.forEach { sex ->
                    FilterChip(
                        selected = state.sex == sex,
                        onClick = { viewModel.setSex(sex) },
                        label = { Text(sex.label) }
                    )
                }
            }

            Divider16()
            FitzpatrickSelector(
                selected = state.fitzpatrick,
                onSelect = { viewModel.setFitzpatrick(it) }
            )

            Divider16()
            BodyMap(
                selected = state.region,
                onSelect = { viewModel.setRegion(it) }
            )

            Divider16()
            OutlinedTextField(
                value = state.lesionSizeText,
                onValueChange = { viewModel.setLesionSize(it) },
                label = { Text("Lezyon Boyutu (mm)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                supportingText = { Text("Tahmini büyüklük. 6mm üstü dikkat gerektirir (ABCDE).") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.familyHistory,
                    onCheckedChange = { viewModel.setFamilyHistory(it) }
                )
                Text(
                    text = "Ailede melanom öyküsü var",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (viewModel.validateAndSave()) onNext()
                },
                enabled = state.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Kameraya Geç", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
