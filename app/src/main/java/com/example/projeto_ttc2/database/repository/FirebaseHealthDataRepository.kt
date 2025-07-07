package com.example.projeto_ttc2.database.repository

import com.example.projeto_ttc2.database.entities.*
import kotlinx.coroutines.flow.Flow

interface FirebaseHealthDataRepository {
    suspend fun syncHeartRateData(userId: String, heartRateData: List<BatimentoCardiaco>)
    suspend fun syncStepsData(userId: String, stepsData: List<Passos>)
    suspend fun syncSleepData(userId: String, sleepData: List<Sono>)
    suspend fun syncCaloriesData(userId: String, caloriesData: List<Calorias>)

    fun getUserHeartRateData(userId: String): Flow<List<BatimentoCardiaco>>
    fun getUserStepsData(userId: String): Flow<List<Passos>>
    fun getUserSleepData(userId: String): Flow<List<Sono>>
    fun getUserCaloriesData(userId: String): Flow<List<Calorias>>
    fun getAllHeartRateData(userId: String): Flow<List<BatimentoCardiaco>>
}