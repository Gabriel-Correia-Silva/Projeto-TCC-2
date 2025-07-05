package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.CaloriasDao
import com.example.projeto_ttc2.database.entities.Calorias
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaloriesRepository @Inject constructor(
    private val caloriasDao: CaloriasDao,
    private val healthConnectManager: HealthConnectManager,
    // CORREÇÃO: Adicionando as dependências ao construtor
    private val firebaseAuth: FirebaseAuth,
    private val firebaseHealthDataRepository: FirebaseHealthDataRepository
) {
    private val TAG = "CaloriesRepository"

    fun getTodayActiveCalories(): Flow<Double> {
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return caloriasDao.getSomaCaloriasPorTipoDesde("ATIVA", startOfDay).map { it ?: 0.0 }
    }

    fun getTodayTotalCalories(): Flow<Double> {
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return caloriasDao.getSomaCaloriasPorTipoDesde("TOTAL", startOfDay).map { it ?: 0.0 }
    }

    suspend fun syncData() {
        val client = healthConnectManager.client
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val now = Instant.now()
        val timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
        val userId = firebaseAuth.currentUser?.uid ?: ""

        try {
            val allEntities = mutableListOf<Calorias>()

            val activeRequest = ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, timeRangeFilter)
            val activeResponse = client.readRecords(activeRequest)
            val activeEntities = activeResponse.records.map { record ->
                Calorias(healthConnectId = record.metadata.id, startTime = record.startTime, endTime = record.endTime, kilocalorias = record.energy.inKilocalories, tipo = "ATIVA", userId = userId)
            }
            if (activeEntities.isNotEmpty()) {
                caloriasDao.insertAll(activeEntities)
                allEntities.addAll(activeEntities)
            }

            val totalRequest = ReadRecordsRequest(TotalCaloriesBurnedRecord::class, timeRangeFilter)
            val totalResponse = client.readRecords(totalRequest)
            val totalEntities = totalResponse.records.map { record ->
                Calorias(healthConnectId = record.metadata.id, startTime = record.startTime, endTime = record.endTime, kilocalorias = record.energy.inKilocalories, tipo = "TOTAL", userId = userId)
            }
            if (totalEntities.isNotEmpty()) {
                caloriasDao.insertAll(totalEntities)
                allEntities.addAll(totalEntities)
            }

            if (userId.isNotEmpty() && allEntities.isNotEmpty()) {
                firebaseHealthDataRepository.syncCaloriesData(userId, allEntities)
            }

            Log.d(TAG, "Sincronização de calorias concluída.")
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de calorias", e)
        }
    }
}