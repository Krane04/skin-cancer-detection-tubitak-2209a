package com.example.melonomscanner

import android.app.Application
import com.example.melonomscanner.data.db.AppDatabase
import com.example.melonomscanner.data.repository.ScanRepository
import com.example.melonomscanner.ml.YoloOnnxRunner
import com.example.melonomscanner.util.Preferences

/**
 * Uygulama genelinde paylaşılan singletonlar.
 *
 * ONNX session ağır olduğu için [yoloRunner] lazy başlatılır; UI katmanı istediğinde
 * tek seferlik kurulur. Aynı session ömür boyu paylaşılır, böylece her tarama
 * modeli yeniden yüklemek zorunda kalmaz.
 */
class MelonomScannerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val scanRepository: ScanRepository by lazy { ScanRepository(database.scanDao()) }
    val preferences: Preferences by lazy { Preferences(this) }

    // Lazy — YoloOnnxRunner ilk tarama/kamera açılışında yüklensin.
    val yoloRunner: YoloOnnxRunner by lazy {
        YoloOnnxRunner(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        runCatching { yoloRunner.close() }
    }

    companion object {
        fun from(context: android.content.Context): MelonomScannerApp =
            context.applicationContext as MelonomScannerApp
    }
}
