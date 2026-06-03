package com.example.melonomscanner.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val imagePath: String,
    val heatmapPath: String? = null,
    val lesionGroupId: String? = null, // aynı lezyonun zaman içindeki taramaları için
    val label: String, // "Nevüs" vs.
    val classCode: String, // "nv"
    val confidence: Float,
    val riskLevel: String, // LOW / MEDIUM / HIGH / CRITICAL
    val age: Int,
    val sex: String,
    val fitzpatrick: Int,
    val region: String,
    val lesionSizeMm: Float? = null,
    val notes: String? = null,
    val flagged: Boolean = false
)
