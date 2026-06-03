package com.example.melonomscanner.ui.screens.metadata

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.melonomscanner.data.model.BodyRegion
import com.example.melonomscanner.data.model.FitzpatrickType
import com.example.melonomscanner.data.model.PatientMetadata
import com.example.melonomscanner.data.model.Sex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MetadataUiState(
    val ageText: String = "",
    val sex: Sex? = null,
    val fitzpatrick: FitzpatrickType? = null,
    val region: BodyRegion? = null,
    val lesionSizeText: String = "",
    val familyHistory: Boolean = false
) {
    val isValid: Boolean
        get() {
            val age = ageText.toIntOrNull() ?: return false
            if (age !in 1..120) return false
            return sex != null && fitzpatrick != null && region != null
        }

    fun toMetadata(): PatientMetadata? {
        if (!isValid) return null
        return PatientMetadata(
            age = ageText.toInt(),
            sex = sex!!,
            fitzpatrick = fitzpatrick!!,
            region = region!!,
            lesionSizeMm = lesionSizeText.toFloatOrNull(),
            familyHistory = familyHistory
        )
    }
}

class MetadataViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(MetadataUiState())
    val state = _state.asStateFlow()

    companion object {
        /** Kamera ekranı tarafından okunacak son kaydedilmiş metadata. */
        @Volatile
        var currentMetadata: PatientMetadata? = null
            private set
    }

    fun setAge(text: String) {
        val filtered = text.filter { it.isDigit() }.take(3)
        _state.update { it.copy(ageText = filtered) }
    }

    fun setSex(value: Sex) = _state.update { it.copy(sex = value) }

    fun setFitzpatrick(value: FitzpatrickType) =
        _state.update { it.copy(fitzpatrick = value) }

    fun setRegion(value: BodyRegion) =
        _state.update { it.copy(region = value) }

    fun setLesionSize(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
            .replace(',', '.')
        _state.update { it.copy(lesionSizeText = filtered) }
    }

    fun setFamilyHistory(value: Boolean) =
        _state.update { it.copy(familyHistory = value) }

    fun validateAndSave(): Boolean {
        val metadata = _state.value.toMetadata() ?: return false
        currentMetadata = metadata
        return true
    }
}
