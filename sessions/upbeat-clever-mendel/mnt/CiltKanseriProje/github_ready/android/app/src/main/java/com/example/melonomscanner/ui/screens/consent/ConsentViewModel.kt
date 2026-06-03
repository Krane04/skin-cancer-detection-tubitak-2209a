package com.example.melonomscanner.ui.screens.consent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melonomscanner.MelonomScannerApp
import kotlinx.coroutines.launch

class ConsentViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = MelonomScannerApp.from(application).preferences

    fun accept() {
        viewModelScope.launch { prefs.setConsentGiven(true) }
    }
}
