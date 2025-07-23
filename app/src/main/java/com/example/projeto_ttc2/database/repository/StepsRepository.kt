package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.PassosDao
import com.example.projeto_ttc2.database.entities.Passos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await 
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepsRepository @Inject constructor(
    private val passosDao: PassosDao,
    private val healthConnectManager: HealthConnectManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseHealthDataRepository: FirebaseHealthDataRepository
) {
    private val TAG = "StepsRepository"
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE 

    fun getTodayStepsFlow(): Flow<Long> {
        val today = LocalDate.now().format(dateFormatter)
        return passosDao.getPassosPorData(today).map { it?.contagem ?: 0L }
    }

    fun getStepsForDate(date: LocalDate): Flow<Passos?> {
        return passosDao.getPassosPorData(date.format(dateFormatter))
    }

    fun getStepsForPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<Passos>> {
        return passosDao.getStepsInPeriod(startDate.format(dateFormatter), endDate.format(dateFormatter))
    }

    suspend fun getHourlyStepsForDate(date: LocalDate): Map<Int, Long> {
        val client = healthConnectManager.client
        val startOfDay = date.atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)

        return try {
            val request = ReadRecordsRequest(StepsRecord::class, timeRangeFilter)
            val response = client.readRecords(request)
            response.records
                .groupBy { record ->
                    record.startTime.atZone(ZoneId.systemDefault()).hour
                }
                .mapValues { entry ->
                    entry.value.sumOf { it.count }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao buscar passos por hora do Health Connect", e)
            emptyMap()
        }
    }

    suspend fun syncData(batch: WriteBatch) {
        val client = healthConnectManager.client
        val startTime = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val endTime = Instant.now()

        try {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = client.readRecords(request)
            val userId = firebaseAuth.currentUser?.uid ?: ""

            val stepsByDay = response.records.groupBy { record ->
                record.startTime.atZone(record.startZoneOffset ?: ZonedDateTime.now().offset).toLocalDate()
            }.mapValues { entry ->
                entry.value.sumOf { it.count }
            }

            val passosEntities = stepsByDay.map { (date, totalSteps) ->
                Passos(data = date.format(dateFormatter), contagem = totalSteps, userId = userId)
            }

            if (passosEntities.isNotEmpty()) {
                passosEntities.forEach { passosDao.upsert(it) }
                Log.d(TAG, "Sincronização de passos concluída. ${stepsByDay.size} dias processados.")

                if (userId.isNotEmpty()) {
                    firebaseHealthDataRepository.syncStepsData(userId, passosEntities, batch)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de passos", e)
        }
    }


    suspend fun upsertStepsFromBle(passos: Passos) {
        try {
            passosDao.upsert(passos)
            Log.d(TAG, "Passos do BLE upserted no Room: ${passos.contagem} para ${passos.data}")

            val userId = firebaseAuth.currentUser?.uid
            if (userId != null && userId.isNotEmpty()) {
                val batch = firebaseHealthDataRepository.firestore.batch()
                firebaseHealthDataRepository.syncStepsData(userId, listOf(passos), batch)
                batch.commit().await()
                Log.d(TAG, "Passos do BLE sincronizados com Firestore: ${passos.contagem}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao fazer upsert de passos do BLE: ${e.message}", e)
        }
    }
}