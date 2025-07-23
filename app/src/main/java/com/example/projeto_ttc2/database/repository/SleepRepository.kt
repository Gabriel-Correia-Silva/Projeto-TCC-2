package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.SleepStageDao
import com.example.projeto_ttc2.database.dao.SonoDao
import com.example.projeto_ttc2.database.entities.SleepStage
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.entities.SonoWithStages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val sonoDao: SonoDao,
    private val sleepStageDao: SleepStageDao,
    private val healthConnectManager: HealthConnectManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseHealthDataRepository: FirebaseHealthDataRepository
) {
    private val TAG = "SleepRepository"

    fun getLatestSleepSession(): Flow<Sono?> {
        return sonoDao.getUltimaSessaoSono()
    }

    fun getLatestSleepSessionWithStages(): Flow<SonoWithStages?> {
        return sonoDao.getUltimaSessaoSonoComEstagios()
    }

    suspend fun syncData(batch: WriteBatch) {
        val client = healthConnectManager.client
        val startTime = Instant.now().minus(48, ChronoUnit.HOURS)
        val endTime = Instant.now()

        try {
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = client.readRecords(request)
            val userId = firebaseAuth.currentUser?.uid ?: ""

            val sonoEntities = mutableListOf<Sono>()
            val stageEntities = mutableListOf<SleepStage>()

            for (record in response.records) {
                sonoEntities.add(
                    Sono(
                        healthConnectId = record.metadata.id,
                        startTime = Date.from(record.startTime),
                        endTime = Date.from(record.endTime),
                        durationMinutes = java.time.Duration.between(record.startTime, record.endTime).toMinutes(),
                        awakeDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_AWAKE }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() },
                        remSleepDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_REM }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() },
                        deepSleepDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_DEEP }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() },
                        lightSleepDurationMinutes = record.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_LIGHT }.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() },
                        userId = userId
                    )
                )
                stageEntities.addAll(record.stages.map {
                    SleepStage(
                        sessionId = record.metadata.id,
                        type = it.stage,
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                })
            }

            if (sonoEntities.isNotEmpty()) {
                sonoDao.insertAll(sonoEntities)
                sleepStageDao.insertAll(stageEntities)
                Log.d(TAG, "${sonoEntities.size} sessões de sono e ${stageEntities.size} estágios inseridos/atualizados.")

                if (userId.isNotEmpty()) {
                    firebaseHealthDataRepository.syncSleepData(userId, sonoEntities, batch)
                }
            } else {
                Log.d(TAG, "Nenhuma nova sessão de sono encontrada.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de sono", e)
        }


    }
}