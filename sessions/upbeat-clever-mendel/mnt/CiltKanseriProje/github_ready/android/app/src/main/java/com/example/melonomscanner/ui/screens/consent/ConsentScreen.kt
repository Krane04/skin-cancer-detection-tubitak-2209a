package com.example.melonomscanner.ui.screens.consent

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ConsentScreen(
    onAccepted: () -> Unit,
    viewModel: ConsentViewModel = viewModel()
) {
    var agreed by remember { mutableStateOf(false) }
    var minor by remember { mutableStateOf(false) }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
        ) {
            Text(
                text = "Aydınlatılmış Onam",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Devam etmeden önce lütfen dikkatlice okuyun.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Section("1. Uygulamanın Amacı",
                        "MelonomScanner, TÜBİTAK 2209-A kapsamında cilt lezyonlarının erken tespitine yardımcı olmak için geliştirilmiş bir araştırma uygulamasıdır. Tıbbi tanı yerine geçmez; kesin teşhis yalnızca bir dermatoloğa aittir.")
                    Section("2. Veri İşleme",
                        "Çekilen tüm görüntüler ve girdiğiniz bilgiler yalnızca cihazınızda saklanır. Hiçbir veri internet üzerinden gönderilmez. Uygulama internet izni talep etmez.")
                    Section("3. KVKK ve GDPR Uyumu",
                        "6698 sayılı Kişisel Verilerin Korunması Kanunu ve GDPR gereği, verileriniz size aittir. Uygulama içinden istediğiniz zaman tüm verileri silebilirsiniz.")
                    Section("4. Model Hakkında",
                        "Kullanılan model HAM10000 veri seti ile eğitilmiş YOLO + FT-Transformer mimarisidir. Duyarlılık/özgüllük metrikleri araştırma ortamında ölçülmüştür; klinik ortamda doğrulama gerektirir.")
                    Section("5. Sorumluluk Reddi",
                        "Uygulamanın ürettiği çıktılar bilgilendirme amaçlıdır. Bir tıbbi karar verirken mutlaka yetkili bir hekime danışın.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agreed, onCheckedChange = { agreed = it })
                Text(
                    text = "Yukarıdaki bilgileri okudum, anladım ve onaylıyorum.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = minor, onCheckedChange = { minor = it })
                Text(
                    text = "18 yaşından büyüğüm veya yetişkin bir kullanıcı olarak uygulamayı kullanıyorum.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* reddet -> çık */ },
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text("Reddet") }
                Button(
                    onClick = {
                        viewModel.accept()
                        onAccepted()
                    },
                    enabled = agreed && minor,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text("Kabul Et") }
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
}
