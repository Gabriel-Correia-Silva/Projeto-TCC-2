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
    private val caloriesRepository: CaloriesRepository,
    private val oxygenSaturationRepository: OxygenSaturationRepository
) {
    private val TAG = "SyncRepository"

    suspend fun syncAllData() {
        try {
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
                val oxygenJob = async {
                    Log.d(TAG, "Sincronizando Oxigenação...")
                    oxygenSaturationRepository.syncData()
                    Log.i(TAG, "Oxigenação OK.")
                }

                awaitAll(heartRateJob, stepsJob, sleepJob, caloriesJob, oxygenJob)

                Log.d(TAG, "--- SINCRONIZAÇÃO COMPLETA CONCLUÍDA COM SUCESSO ---")
            }
        } catch (e: Exception) {
            Log.e(TAG, "--- FALHA GERAL NA SINCRONIZAÇÃO ---", e)
            throw e
        }
    }
}