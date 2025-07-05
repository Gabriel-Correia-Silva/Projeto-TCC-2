package com.example.projeto_ttc2.database.repository

import android.util.Log
import com.example.projeto_ttc2.database.entities.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseHealthDataRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirebaseHealthDataRepository {

    private val TAG = "FirebaseHealthDataRepo"

    // Estrutura no Firestore: /users/{userId}/heart_rate/{documentId}
    override suspend fun syncHeartRateData(userId: String, heartRateData: List<BatimentoCardiaco>) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("heart_rate")
        val batch = firestore.batch()
        for (data in heartRateData) {
            // Usamos o timestamp como ID para evitar duplicatas
            val docRef = collection.document(data.timestamp.toEpochMilli().toString())
            batch.set(docRef, data)
        }
        batch.commit().await()
        Log.d(TAG, "${heartRateData.size} registros de frequência cardíaca sincronizados para o usuário $userId")
    }

    override suspend fun syncStepsData(userId: String, stepsData: List<Passos>) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("steps")
        val batch = firestore.batch()
        for (data in stepsData) {
            // Usamos a data como ID
            val docRef = collection.document(data.data.toString())
            batch.set(docRef, data)
        }
        batch.commit().await()
        Log.d(TAG, "${stepsData.size} registros de passos sincronizados para o usuário $userId")
    }

    override suspend fun syncSleepData(userId: String, sleepData: List<Sono>) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("sleep")
        val batch = firestore.batch()
        for (data in sleepData) {
            val docRef = collection.document(data.healthConnectId)
            batch.set(docRef, data)
        }
        batch.commit().await()
        Log.d(TAG, "${sleepData.size} registros de sono sincronizados para o usuário $userId")
    }

    override suspend fun syncCaloriesData(userId: String, caloriesData: List<Calorias>) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("calories")
        val batch = firestore.batch()
        for (data in caloriesData) {
            val docRef = collection.document(data.healthConnectId)
            batch.set(docRef, data)
        }
        batch.commit().await()
        Log.d(TAG, "${caloriesData.size} registros de calorias sincronizados para o usuário $userId")
    }

    override suspend fun getUserHeartRateData(userId: String): Flow<List<BatimentoCardiaco>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("heart_rate").get().await()
            emit(snapshot.toObjects())
        } else {
            emit(emptyList())
        }
    }

    override suspend fun getUserStepsData(userId: String): Flow<List<Passos>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("steps").get().await()
            emit(snapshot.toObjects())
        } else {
            emit(emptyList())
        }
    }

    override suspend fun getUserSleepData(userId: String): Flow<List<Sono>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("sleep").get().await()
            emit(snapshot.toObjects())
        } else {
            emit(emptyList())
        }
    }

    override suspend fun getUserCaloriesData(userId: String): Flow<List<Calorias>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("calories").get().await()
            emit(snapshot.toObjects())
        } else {
            emit(emptyList())
        }
    }
}