package com.example.melonomscanner.ui.screens.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melonomscanner.MelonomScannerApp
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = MelonomScannerApp.from(application).preferences

    fun markDone() {
        viewModelScope.launch { prefs.setOnboardingDone(true) }
    }
}
