package com.example.melonomscanner.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melonomscanner.MelonomScannerApp
import com.example.melonomscanner.ui.theme.ThemeOption
import com.example.melonomscanner.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = MelonomScannerApp.from(application)
    private val prefs = app.preferences
    private val repo = app.scanRepository

    val theme: Flow<ThemeOption> = prefs.theme
    val dynamicColor: Flow<Boolean> = prefs.dynamicColor
    val biometricLock: Flow<Boolean> = prefs.biometricLock
    val haptics: Flow<Boolean> = prefs.hapticsEnabled

    fun setTheme(option: ThemeOption) = viewModelScope.launch { prefs.setTheme(option) }
    fun setDynamicColor(value: Boolean) = viewModelScope.launch { prefs.setDynamicColor(value) }
    fun setBiometric(value: Boolean) = viewModelScope.launch { prefs.setBiometricLock(value) }
    fun setHaptics(value: Boolean) = viewModelScope.launch { prefs.setHaptics(value) }

    fun deleteAllData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.deleteAll()
                ImageUtils.scansDir(getApplication()).listFiles()?.forEach { it.delete() }
                ImageUtils.heatmapsDir(getApplication()).listFiles()?.forEach { it.delete() }
            }
        }
    }
}
