package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.PassosDao
import com.example.projeto_ttc2.database.entities.Passos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepsRepository @Inject constructor(
    private val passosDao: PassosDao,
    private val healthConnectManager: HealthConnectManager,
) {
    private val TAG = "StepsRepository"

    fun getTodayStepsFlow(): Flow<Long> {
        return passosDao.getPassosPorData(LocalDate.now()).map { it?.contagem ?: 0L }
    }

    suspend fun syncData() {
        val client = healthConnectManager.client
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val now = Instant.now()
        val request = ReadRecordsRequest(StepsRecord::class, TimeRangeFilter.between(startOfDay, now))

        try {
            val response = client.readRecords(request)
            val totalPassos = response.records.sumOf { it.count }
            passosDao.upsert(Passos(data = LocalDate.now(), contagem = totalPassos))
            Log.d(TAG, "Dados de passos salvos para hoje: $totalPassos")
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de passos", e)
        }
    }
}