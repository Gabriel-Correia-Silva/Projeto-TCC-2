package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateRepository @Inject constructor(
    private val batimentoCardiacoDao: BatimentoCardiacoDao,
    private val healthConnectManager: HealthConnectManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseHealthDataRepository: FirebaseHealthDataRepository
) {
    private val TAG = "HeartRateRepository"

    fun getLatestHeartRate(): Flow<Long> {
        return batimentoCardiacoDao.getUltimoBatimento().map { it?.bpm ?: 0L }
    }

    fun getTodayHeartRateData(): Flow<List<Long>> {
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return batimentoCardiacoDao.getBatimentosDesdeInicioDoDia(startOfDay)
            .map { batimentos ->
                batimentos.map { it.bpm }
            }
    }

    fun getHeartRateRecordsForDate(date: LocalDate): Flow<List<BatimentoCardiaco>> {
        val startOfDay = date.atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return batimentoCardiacoDao.getBatimentosDoPeriodo(startOfDay, endOfDay)
    }

    fun getTodayHeartRateRecords(): Flow<List<BatimentoCardiaco>> {
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return batimentoCardiacoDao.getBatimentosDesdeInicioDoDia(startOfDay)
    }

    suspend fun syncData() {
        val client = healthConnectManager.client
        val endTime = Instant.now()

        val startTime = endTime.minus(24, ChronoUnit.HOURS)
        val request = ReadRecordsRequest(HeartRateRecord::class, TimeRangeFilter.between(startTime, endTime))

        try {
            val response = client.readRecords(request)
            Log.d(TAG, "Health Connect retornou ${response.records.size} registros de frequência cardíaca.")

            val userId = firebaseAuth.currentUser?.uid ?: ""

            val entities = response.records.flatMap { record ->
                record.samples.map { sample ->
                    BatimentoCardiaco(
                        timestamp = sample.time.toEpochMilli(),
                        healthConnectId = record.metadata.id + "_" + sample.time.toEpochMilli(),
                        bpm = sample.beatsPerMinute,
                        zoneOffset = record.startZoneOffset?.id,
                        userId = userId
                    )
                }
            }

            if (entities.isNotEmpty()) {
                Log.i(TAG, "Enviando ${entities.size} registros de batimentos para o Firestore.")
                batimentoCardiacoDao.insertAll(entities)
                if (userId.isNotEmpty()) {
                    firebaseHealthDataRepository.syncHeartRateData(userId, entities)
                }
            } else {
                Log.w(TAG, "Nenhum novo dado de batimento cardíaco para sincronizar.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de frequência cardíaca", e)
        }
    }
}