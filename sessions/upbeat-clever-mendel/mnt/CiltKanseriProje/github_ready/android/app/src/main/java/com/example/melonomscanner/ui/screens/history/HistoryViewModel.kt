package com.example.melonomscanner.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.melonomscanner.MelonomScannerApp
import com.example.melonomscanner.data.db.ScanEntity
import kotlinx.coroutines.flow.Flow

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MelonomScannerApp.from(application).scanRepository

    val scans: Flow<List<ScanEntity>> = repo.observeAll()
}
