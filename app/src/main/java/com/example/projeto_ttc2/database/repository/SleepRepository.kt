package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.SonoDao
import com.example.projeto_ttc2.database.entities.Sono
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val sonoDao: SonoDao,
    private val healthConnectManager: HealthConnectManager
) {
    private val TAG = "SleepRepository"

    fun getLatestSleepSession(): Flow<Sono?> {
        return sonoDao.getUltimaSessaoSono()
    }

    suspend fun syncData() {
        val client = healthConnectManager.client
        val startTime = Instant.now().minus(48, ChronoUnit.HOURS)
        val endTime = Instant.now()

        try {
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = client.readRecords(request)
            val sonoEntities = response.records.map { record ->
                Sono(
                    healthConnectId = record.metadata.id,
                    startTime = record.startTime,
                    endTime = record.endTime,
                    durationMinutes = java.time.Duration.between(record.startTime, record.endTime).toMinutes(),
                    awakeDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_AWAKE }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() },
                    remSleepDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_REM }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() },
                    deepSleepDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_DEEP }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() },
                    lightSleepDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_LIGHT }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                )
            }
            if (sonoEntities.isNotEmpty()) {
                sonoDao.insertAll(sonoEntities)
                Log.d(TAG, "${sonoEntities.size} sessões de sono inseridas/atualizadas.")
            } else {
                Log.d(TAG, "Nenhuma nova sessão de sono encontrada.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de sono", e)
        }
    }
}