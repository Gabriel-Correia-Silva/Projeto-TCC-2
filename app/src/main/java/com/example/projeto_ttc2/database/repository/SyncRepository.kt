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
            // Adicionando logs detalhados para cada etapa da sincronização
            Log.d(TAG, "--- INICIANDO SINCRONIZAÇÃO COMPLETA ---")

            coroutineScope {
                val heartRateJob = async {
                    Log.d(TAG, "Sincronizando Frequência Cardíaca...")
                    heartRateRepository.syncData()
                    Log.i(TAG, "Frequência Cardíaca OK.")
                }
                val stepsJob = async {
                    Log.d(TAG, "Sincronizando Passos...")
                    stepsRepository.syncData()
                    Log.i(TAG, "Passos OK.")
                }
                val sleepJob = async {
                    Log.d(TAG, "Sincronizando Sono...")
                    sleepRepository.syncData()
                    Log.i(TAG, "Sono OK.")
                }
                val caloriesJob = async {
                    Log.d(TAG, "Sincronizando Calorias...")
                    caloriesRepository.syncData()
                    Log.i(TAG, "Calorias OK.")
                }

                // Aguarda a conclusão de todas as tarefas de sincronização
                awaitAll(heartRateJob, stepsJob, sleepJob, caloriesJob)

                Log.d(TAG, "--- SINCRONIZAÇÃO COMPLETA CONCLUÍDA COM SUCESSO ---")
            }
        } catch (e: Exception) {
            Log.e(TAG, "--- FALHA GERAL NA SINCRONIZAÇÃO ---", e)
            // Propaga a exceção para que o chamador (Worker ou ViewModel) possa lidar com ela.
            throw e
        }
    }
}