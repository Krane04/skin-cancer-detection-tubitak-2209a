package com.example.melonomscanner.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.melonomscanner.MelonomScannerApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MelonomScannerApp.from(application).scanRepository

    val totalScans: Flow<Int> = repo.observeCount()
    val followedLesions: Flow<Int> = repo.observeFollowed()
    val flaggedCount: Flow<Int> = repo.observeFlagged().map { it.size }
}
