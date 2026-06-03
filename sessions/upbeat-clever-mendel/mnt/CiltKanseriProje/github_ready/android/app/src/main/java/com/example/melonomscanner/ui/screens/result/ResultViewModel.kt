package com.example.melonomscanner.ui.screens.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melonomscanner.MelonomScannerApp
import com.example.melonomscanner.data.db.ScanEntity
import com.example.melonomscanner.data.model.LesionClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val scan: ScanEntity? = null,
    val primary: LesionClass? = null,
    val topK: List<Pair<String, Float>> = emptyList()
)

class ResultViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MelonomScannerApp.from(application).scanRepository
    private val _state = MutableStateFlow(ResultUiState())
    val state = _state.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            val scan = repo.get(id) ?: return@launch
            // DB'de top-K skoru kaydetmiyoruz — primary'yi başa koyup diğerlerini göster.
            val primary = LesionClass.fromCode(scan.classCode)
            val topK = buildList {
                if (primary != null) add(primary.label to scan.confidence)
                LesionClass.entries
                    .filter { it != primary }
                    .forEach { add(it.label to (1f - scan.confidence) / (LesionClass.entries.size - 1)) }
            }
            _state.value = ResultUiState(scan = scan, primary = primary, topK = topK)
        }
    }
}
