package com.example.projeto_ttc2.database.repository

import android.util.Log
import com.example.projeto_ttc2.database.entities.*
import com.example.projeto_ttc2.network.ApiService
import com.example.projeto_ttc2.network.DetailedHealthAndSensorPayload
import com.example.projeto_ttc2.network.HourlyStepsPayload
import com.example.projeto_ttc2.network.SleepSessionPayload
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val heartRateRepository: HeartRateRepository,
    private val stepsRepository: StepsRepository,
    private val sleepRepository: SleepRepository,
    private val caloriesRepository: CaloriesRepository,
    private val oxygenSaturationRepository: OxygenSaturationRepository,
    private val sensorRepository: SensorRepository,
    private val apiService: ApiService,
    private val firebaseAuth: FirebaseAuth
) {
    private val TAG = "SyncRepository"

    suspend fun syncAllData() {
        Log.d(TAG, "--- INICIANDO SINCRONIZAÇÃO DETALHADA ---")

        try {
            val userId = firebaseAuth.currentUser?.uid ?: run {
                Log.w(TAG, "ID do utilizador nulo. A cancelar sincronização.")
                return
            }


            val batch = firestore.batch()
            val sensorReadings: Pair<List<AccelerometerData>, List<GyroscopeData>>

            coroutineScope {

                val heartRateJob = async { heartRateRepository.syncData(batch) }
                val stepsJob = async { stepsRepository.syncData(batch) }
                val sleepJob = async { sleepRepository.syncData(batch) }
                val caloriesJob = async { caloriesRepository.syncData(batch) }
                val oxygenJob = async { oxygenSaturationRepository.syncData(batch) }


                val sensorJob = async {
                    withTimeoutOrNull(6000L) {
                        sensorRepository.captureSensorData(5000L).firstOrNull()
                    } ?: run {
                        Log.w(TAG, "Timeout na captura de sensores, a usar listas vazias.")
                        Pair(emptyList(), emptyList())
                    }
                }

                awaitAll(heartRateJob, stepsJob, sleepJob, caloriesJob, oxygenJob)
                sensorReadings = sensorJob.await()
            }
            Log.d(TAG, "Sincronização local e captura de sensores concluídas.")

            try {
                Log.d(TAG, "A montar o payload para a API Web...")
                val latestSleepWithStages = sleepRepository.getLatestSleepSessionWithStages().firstOrNull()
                val sleepPayload = if (latestSleepWithStages?.sono != null) {
                    listOf(SleepSessionPayload(latestSleepWithStages.sono, latestSleepWithStages.stages))
                } else {
                    emptyList()
                }

                val today = LocalDate.now()
                val hourlySteps = stepsRepository.getHourlyStepsForDate(today)
                val stepsPayload = HourlyStepsPayload(today.format(DateTimeFormatter.ISO_LOCAL_DATE), hourlySteps)
                val heartRateToday = heartRateRepository.getTodayHeartRateRecords().firstOrNull() ?: emptyList()

                val caloriesToday = caloriesRepository.getTodayTotalCalories().firstOrNull()?.let {
                    if (it > 0) listOf(Calorias(kilocalorias = it, userId = userId)) else emptyList()
                } ?: emptyList()

                val oxygenToday = oxygenSaturationRepository.getLatestSevenOxygenationReadings().firstOrNull()?.map {
                    OxigenacaoSanguinea(spo2 = it, userId = userId)
                } ?: emptyList()

                val detailedPayload = DetailedHealthAndSensorPayload(
                    userId = userId,
                    timestamp = System.currentTimeMillis(),
                    heartRateRecords = heartRateToday,
                    steps = stepsPayload,
                    sleepSessions = sleepPayload,
                    calorieRecords = caloriesToday,
                    oxygenSaturationRecords = oxygenToday,
                    accelerometerReadings = sensorReadings.first,
                    gyroscopeReadings = sensorReadings.second
                )

                Log.d(TAG, "A enviar o payload para a API Web...")
                val response = apiService.uploadDetailedHealthData(detailedPayload)
                if (response.isSuccessful) {
                    Log.d(TAG, "✓ Payload enviado com sucesso para a API Web.")
                } else {
                    Log.e(TAG, "✗ Falha ao enviar para a API Web: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Erro de comunicação com a API Web.", e)
            }


            try {
                Log.d(TAG, "A tentar enviar o lote para o Firestore (uma única vez)...")
                batch.commit().await()
                Log.d(TAG, "✓ Dados enviados com sucesso para o Firestore.")
            } catch (e: Exception) {
                Log.w(TAG, "✗ Falha ao enviar para o Firestore (ignorado): ${e.message}")
            }

            Log.d(TAG, "--- SINCRONIZAÇÃO CONCLUÍDA ---")

        } catch (e: Exception) {
            Log.e(TAG, "--- FALHA CRÍTICA NA SINCRONIZAÇÃO ---", e)
            throw e // Relança a excepção para que o WorkManager possa tentar novamente mais tarde
        }
    }
}