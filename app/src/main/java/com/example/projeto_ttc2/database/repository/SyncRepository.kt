package com.example.projeto_ttc2.database.repository

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val heartRateRepository: HeartRateRepository,
    private val stepsRepository: StepsRepository,
    private val sleepRepository: SleepRepository,
    private val caloriesRepository: CaloriesRepository
) {
    private val TAG = "SyncRepository"

    suspend fun syncAllData() {
        try {
            coroutineScope {
                val syncTasks = listOf(
                    async { heartRateRepository.syncData() },
                    async { stepsRepository.syncData() },
                    async { sleepRepository.syncData() },
                    async { caloriesRepository.syncData() }
                )
                syncTasks.awaitAll()
                Log.d(TAG, "Sincronização de todos os dados concluída com sucesso.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar todos os dados.", e)
            // Propaga a exceção para que o chamador (Worker ou ViewModel) possa lidar com ela.
            throw e
        }
    }
}