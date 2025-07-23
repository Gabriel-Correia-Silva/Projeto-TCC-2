package com.example.projeto_ttc2.database.repository

import android.util.Log
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.OxigenacaoSanguineaDao
import com.example.projeto_ttc2.database.entities.OxigenacaoSanguinea
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await // Importar await
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OxygenSaturationRepository @Inject constructor(
    private val oxigenacaoSanguineaDao: OxigenacaoSanguineaDao,
    private val healthConnectManager: HealthConnectManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseHealthDataRepository: FirebaseHealthDataRepository
) {
    private val TAG = "OxygenSaturationRepo"

    fun getLatestOxygenSaturation(): Flow<Double> {
        return oxigenacaoSanguineaDao.getUltimaOxigenacao().map { it?.spo2 ?: 0.0 }
    }
    fun getLatestSevenOxygenationReadings(): Flow<List<Double>> {
        return oxigenacaoSanguineaDao.getUltimasSeteOxigenacoes().map { records ->
            records.map { it.spo2 }
        }
    }

    suspend fun syncData(batch: WriteBatch) {
        val client = healthConnectManager.client
        val endTime = Instant.now()
        val startTime = endTime.minus(15, ChronoUnit.DAYS)
        val request = ReadRecordsRequest(OxygenSaturationRecord::class, TimeRangeFilter.between(startTime, endTime))

        try {
            val response = client.readRecords(request)
            Log.d(TAG, "Health Connect retornou ${response.records.size} registros de SpO2.")

            val userId = firebaseAuth.currentUser?.uid ?: ""

            val entities = response.records.map { record ->
                OxigenacaoSanguinea(
                    timestamp = record.time.toEpochMilli(),
                    healthConnectId = record.metadata.id,
                    spo2 = record.percentage.value,
                    zoneOffset = record.zoneOffset?.id,
                    userId = userId
                )
            }

            if (entities.isNotEmpty()) {
                oxigenacaoSanguineaDao.insertAll(entities)
                if (userId.isNotEmpty()) {
                    firebaseHealthDataRepository.syncOxygenSaturationData(userId, entities, batch)
                }

                val fifteenDaysAgo = Instant.now().minus(15, ChronoUnit.DAYS).toEpochMilli()
                oxigenacaoSanguineaDao.deleteOldData(fifteenDaysAgo)
                Log.d(TAG, "Dados de SpO2 antigos foram removidos.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de SpO2", e)
        }
    }

    suspend fun insertOxygenSaturationFromBle(oxigenacao: OxigenacaoSanguinea) {
        try {
            oxigenacaoSanguineaDao.insertAll(listOf(oxigenacao))
            Log.d(TAG, "Oxigenação do BLE inserida no Room: ${oxigenacao.spo2}")

            val userId = firebaseAuth.currentUser?.uid
            if (userId != null && userId.isNotEmpty()) {
                val batch = firebaseHealthDataRepository.firestore.batch()
                firebaseHealthDataRepository.syncOxygenSaturationData(userId, listOf(oxigenacao), batch)
                batch.commit().await()
                Log.d(TAG, "Oxigenação do BLE sincronizada com Firestore: ${oxigenacao.spo2}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao inserir oxigenação do BLE: ${e.message}", e)
        }
    }
}