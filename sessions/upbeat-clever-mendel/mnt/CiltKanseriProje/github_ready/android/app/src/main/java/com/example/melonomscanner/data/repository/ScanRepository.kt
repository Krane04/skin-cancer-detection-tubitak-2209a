package com.example.melonomscanner.data.repository

import com.example.melonomscanner.data.db.ScanDao
import com.example.melonomscanner.data.db.ScanEntity
import com.example.melonomscanner.data.model.PatientMetadata
import com.example.melonomscanner.data.model.ScanOutcome
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ScanRepository(private val dao: ScanDao) {

    fun observeAll(): Flow<List<ScanEntity>> = dao.observeAll()
    fun observeFlagged(): Flow<List<ScanEntity>> = dao.observeFlagged()
    fun observeCount(): Flow<Int> = dao.observeCount()
    fun observeFollowed(): Flow<Int> = dao.observeFollowedLesionCount()

    suspend fun save(
        outcome: ScanOutcome,
        metadata: PatientMetadata,
        imagePath: String,
        heatmapPath: String? = null,
        flagged: Boolean = false,
        groupId: String? = null,
        notes: String? = null
    ): Long {
        val entity = ScanEntity(
            timestamp = System.currentTimeMillis(),
            imagePath = imagePath,
            heatmapPath = heatmapPath,
            lesionGroupId = groupId ?: UUID.randomUUID().toString(),
            label = outcome.primary.label,
            classCode = outcome.primary.code,
            confidence = outcome.confidence,
            riskLevel = outcome.riskLevel.name,
            age = metadata.age,
            sex = metadata.sex.name,
            fitzpatrick = metadata.fitzpatrick.level,
            region = metadata.region.code,
            lesionSizeMm = metadata.lesionSizeMm,
            notes = notes,
            flagged = flagged
        )
        return dao.insert(entity)
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun get(id: Long): ScanEntity? = dao.getById(id)
    suspend fun history(groupId: String): List<ScanEntity> = dao.getByGroup(groupId)
}
