package com.example.melonomscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.melonomscanner.ui.screens.camera.CameraScreen
import com.example.melonomscanner.ui.screens.consent.ConsentScreen
import com.example.melonomscanner.ui.screens.history.HistoryScreen
import com.example.melonomscanner.ui.screens.home.HomeScreen
import com.example.melonomscanner.ui.screens.info.AbcdeScreen
import com.example.melonomscanner.ui.screens.metadata.MetadataScreen
import com.example.melonomscanner.ui.screens.onboarding.OnboardingScreen
import com.example.melonomscanner.ui.screens.result.ResultScreen
import com.example.melonomscanner.ui.screens.settings.SettingsScreen
import com.example.melonomscanner.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val CONSENT = "consent"
    const val HOME = "home"
    const val METADATA = "metadata"
    const val CAMERA = "camera"
    const val RESULT = "result/{scanId}"
    const val HISTORY = "history"
    const val ABCDE = "abcde"
    const val SETTINGS = "settings"

    fun result(scanId: Long) = "result/$scanId"
}

@Composable
fun MelonomNavHost(
    onboardingDone: Boolean,
    consentGiven: Boolean
) {
    val navController = rememberNavController()

    // Splash sonrası gidilecek route — kullanıcı durumuna göre
    val postSplashDestination = when {
        !onboardingDone -> Routes.ONBOARDING
        !consentGiven -> Routes.CONSENT
        else -> Routes.HOME
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onTimeout = {
                navController.navigate(postSplashDestination) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                navController.navigate(Routes.CONSENT) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.CONSENT) {
            ConsentScreen(onAccepted = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.CONSENT) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onNewScan = { navController.navigate(Routes.METADATA) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onInfo = { navController.navigate(Routes.ABCDE) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.METADATA) {
            MetadataScreen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.CAMERA) }
            )
        }
        composable(Routes.CAMERA) {
            CameraScreen(
                onBack = { navController.popBackStack() },
                onScanComplete = { scanId ->
                    navController.navigate(Routes.result(scanId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            Routes.RESULT,
            arguments = listOf(navArgument("scanId") { type = NavType.LongType })
        ) { entry ->
            val scanId = entry.arguments?.getLong("scanId") ?: 0L
            ResultScreen(
                scanId = scanId,
                onBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpen = { scanId -> navController.navigate(Routes.result(scanId)) }
            )
        }
        composable(Routes.ABCDE) {
            AbcdeScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
