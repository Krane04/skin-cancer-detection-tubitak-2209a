package com.example.melonomscanner.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melonomscanner.R
import kotlinx.coroutines.delay

/**
 * Brand splash ekranı (Compose).
 *
 * Akış:
 *  1) Logo fade-in + scale-up (700 ms)
 *  2) Slogan fade-in (400 ms)
 *  3) ~1.5 saniye bekleme (toplam görüntülenme ~2.5 sn)
 *  4) Tüm ekran fade-out (300 ms) → onTimeout() çağrılır
 *
 * Sistem splash (Android 12+) bu ekranın hemen öncesinde gösterilir ve
 * teal background korunur, böylece geçiş kesintisiz hissedilir.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val rootAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 1) Logo görünür ol
        logoAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        // 2) Logo büyür
        logoScale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        // 3) Slogan ve metinler gecikmeyle belirir
        delay(400)
        textAlpha.animateTo(1f, animationSpec = tween(400, easing = LinearEasing))
    }
    LaunchedEffect(Unit) {
        // 4) Bekle ve fade-out
        delay(2200)
        rootAlpha.animateTo(0f, animationSpec = tween(300, easing = LinearEasing))
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(rootAlpha.value)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "MelonomScanner",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Cilt Lezyonu Ön Değerlendirme",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(28.dp)
                    .alpha(textAlpha.value),
                strokeWidth = 2.5.dp
            )
        }

        // Alt bilgi (TÜBİTAK desteği)
        Text(
            text = "TÜBİTAK 2209-A • Sakarya Üniversitesi",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(textAlpha.value)
                .padding(bottom = 32.dp)
        )
    }
}
