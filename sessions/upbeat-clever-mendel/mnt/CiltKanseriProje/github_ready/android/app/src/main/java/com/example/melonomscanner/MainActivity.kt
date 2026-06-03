package com.example.melonomscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.melonomscanner.ui.navigation.MelonomNavHost
import com.example.melonomscanner.ui.theme.MelonomScannerTheme
import com.example.melonomscanner.ui.theme.ThemeOption

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = MelonomScannerApp.from(this)

        // Splash'ı preferences yüklenene kadar tut
        var keep = true
        splash.setKeepOnScreenCondition { keep }

        setContent {
            val theme by app.preferences.theme.collectAsStateWithLifecycle(initialValue = ThemeOption.SYSTEM)
            val dynamic by app.preferences.dynamicColor.collectAsStateWithLifecycle(initialValue = true)
            val onboardingDone by app.preferences.onboardingDone.collectAsStateWithLifecycle(initialValue = false)
            val consentGiven by app.preferences.consentGiven.collectAsStateWithLifecycle(initialValue = false)

            // İlk state geldiğinde splash'ı bırak
            keep = false

            MelonomScannerTheme(themeOption = theme, dynamicColor = dynamic) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MelonomNavHost(
                        onboardingDone = onboardingDone,
                        consentGiven = consentGiven
                    )
                }
            }
        }
    }
}
