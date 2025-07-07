package com.example.projeto_ttc2.database.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
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

            // 1. Crie um único batch
            val batch = firestore.batch()

            coroutineScope {
                // 2. Passe o mesmo batch para todas as operações de sincronização
                val heartRateJob = async {
                    Log.d(TAG, "Adicionando Frequência Cardíaca ao lote...")
                    heartRateRepository.syncData(batch)
                    Log.i(TAG, "Frequência Cardíaca pronta para o lote.")
                }
                val stepsJob = async {
                    Log.d(TAG, "Adicionando Passos ao lote...")
                    stepsRepository.syncData(batch)
                    Log.i(TAG, "Passos prontos para o lote.")
                }
                val sleepJob = async {
                    Log.d(TAG, "Adicionando Sono ao lote...")
                    sleepRepository.syncData(batch)
                    Log.i(TAG, "Sono pronto para o lote.")
                }
                val caloriesJob = async {
                    Log.d(TAG, "Adicionando Calorias ao lote...")
                    caloriesRepository.syncData(batch)
                    Log.i(TAG, "Calorias prontas para o lote.")
                }
                val oxygenJob = async {
                    Log.d(TAG, "Adicionando Oxigenação ao lote...")
                    oxygenSaturationRepository.syncData(batch)
                    Log.i(TAG, "Oxigenação pronta para o lote.")
                }

                awaitAll(heartRateJob, stepsJob, sleepJob, caloriesJob, oxygenJob)
            }

            // 3. Faça o commit de todas as gravações de uma só vez
            Log.d(TAG, "Enviando lote único para o Firestore...")
            batch.commit().await()
            Log.d(TAG, "--- SINCRONIZAÇÃO COMPLETA CONCLUÍDA COM SUCESSO ---")

        } catch (e: Exception) {
            Log.e(TAG, "--- FALHA GERAL NA SINCRONIZAÇÃO ---", e)
            throw e
        }
    }
}