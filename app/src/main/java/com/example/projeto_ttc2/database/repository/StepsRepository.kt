package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.PassosDao
import com.example.projeto_ttc2.database.entities.Passos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepsRepository @Inject constructor(
    private val passosDao: PassosDao,
    private val healthConnectManager: HealthConnectManager
) {
    private val TAG = "StepsRepository"

    fun getTodayStepsFlow(): Flow<Long> {
        val today = LocalDate.now()
        return passosDao.getPassosPorData(today).map { it?.contagem ?: 0L }
    }

    suspend fun syncData() {
        val client = healthConnectManager.client
        val startTime = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val endTime = ZonedDateTime.now().toInstant()

        try {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = client.readRecords(request)

            // Agrupa os steps por dia
            val stepsByDay = response.records.groupBy { record ->
                val startDate = record.startTime.atZone(record.startZoneOffset ?: ZonedDateTime.now().offset).toLocalDate()
                startDate
            }.mapValues { entry ->
                entry.value.sumOf { it.count }
            }

            // Insere/atualiza os dados no Room
            stepsByDay.forEach { (date, totalSteps) ->
                val passos = Passos(data = date, contagem = totalSteps)
                passosDao.upsert(passos)
            }

            Log.d(TAG, "Sincronização de passos concluída. ${stepsByDay.size} dias processados.")
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de passos", e)
        }
    }
}
