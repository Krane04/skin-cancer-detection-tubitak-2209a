package com.example.melonomscanner.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.melonomscanner.ui.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Basit DataStore sarmalayıcı.
 * Uygulama tarafından izlenen ayarlar:
 *  - Onboarding tamamlandı mı?
 *  - KVKK onayı verildi mi?
 *  - Tema (system/light/dark)
 *  - Dinamik renk (Material You)
 *  - Biyometrik kilit
 */
class Preferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val CONSENT_GIVEN = booleanPreferencesKey("consent_given")
        val THEME = stringPreferencesKey("theme_option")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ONBOARDING_DONE] ?: false }

    val consentGiven: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.CONSENT_GIVEN] ?: false }

    val theme: Flow<ThemeOption> = context.dataStore.data
        .map {
            runCatching { ThemeOption.valueOf(it[Keys.THEME] ?: ThemeOption.SYSTEM.name) }
                .getOrDefault(ThemeOption.SYSTEM)
        }

    val dynamicColor: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.DYNAMIC_COLOR] ?: true }

    val biometricLock: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.BIOMETRIC_LOCK] ?: false }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HAPTICS] ?: true }

    suspend fun setOnboardingDone(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = value }
    }

    suspend fun setConsentGiven(value: Boolean) {
        context.dataStore.edit { it[Keys.CONSENT_GIVEN] = value }
    }

    suspend fun setTheme(option: ThemeOption) {
        context.dataStore.edit { it[Keys.THEME] = option.name }
    }

    suspend fun setDynamicColor(value: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = value }
    }

    suspend fun setBiometricLock(value: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_LOCK] = value }
    }

    suspend fun setHaptics(value: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS] = value }
    }

    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
