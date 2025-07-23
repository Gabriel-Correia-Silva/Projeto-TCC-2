package com.example.projeto_ttc2.database.repository

import com.example.projeto_ttc2.database.entities.*
import com.google.firebase.firestore.FirebaseFirestore 
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow

interface FirebaseHealthDataRepository {
    val firestore: FirebaseFirestore

    suspend fun syncHeartRateData(userId: String, heartRateData: List<BatimentoCardiaco>, batch: WriteBatch)
    suspend fun syncStepsData(userId: String, stepsData: List<Passos>, batch: WriteBatch)
    suspend fun syncSleepData(userId: String, sleepData: List<Sono>, batch: WriteBatch)
    suspend fun syncCaloriesData(userId: String, caloriesData: List<Calorias>, batch: WriteBatch)
    suspend fun syncOxygenSaturationData(userId: String, oxygenData: List<OxigenacaoSanguinea>, batch: WriteBatch)

    fun getUserHeartRateData(userId: String): Flow<List<BatimentoCardiaco>>
    fun getUserStepsData(userId: String): Flow<List<Passos>>
    fun getUserSleepData(userId: String): Flow<List<Sono>>
    fun getUserCaloriesData(userId: String): Flow<List<Calorias>>
    fun getAllHeartRateData(userId: String): Flow<List<BatimentoCardiaco>>
}