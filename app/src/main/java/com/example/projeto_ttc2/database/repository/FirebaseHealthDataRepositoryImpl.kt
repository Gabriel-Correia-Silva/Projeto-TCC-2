package com.example.projeto_ttc2.database.repository

import android.util.Log
import com.example.projeto_ttc2.database.entities.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseHealthDataRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirebaseHealthDataRepository {


    override suspend fun syncHeartRateData(userId: String, heartRateData: List<BatimentoCardiaco>, batch: WriteBatch) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("heart_rate")
        for (data in heartRateData) {
            val docRef = collection.document(data.timestamp.toString())
            batch.set(docRef, data)
        }
    }

    override suspend fun syncStepsData(userId: String, stepsData: List<Passos>, batch: WriteBatch) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("steps")
        for (data in stepsData) {
            val docRef = collection.document(data.data)
            batch.set(docRef, data)
        }
    }

    override suspend fun syncSleepData(userId: String, sleepData: List<Sono>, batch: WriteBatch) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("sleep")
        for (data in sleepData) {
            val docRef = collection.document(data.healthConnectId)
            batch.set(docRef, data)
        }
    }

    override suspend fun syncCaloriesData(userId: String, caloriesData: List<Calorias>, batch: WriteBatch) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("calories")
        for (data in caloriesData) {
            val docRef = collection.document(data.healthConnectId)
            batch.set(docRef, data)
        }
    }

    override suspend fun syncOxygenSaturationData(userId: String, oxygenData: List<OxigenacaoSanguinea>, batch: WriteBatch) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("oxygen_saturation")
        for (data in oxygenData) {
            val docRef = collection.document(data.timestamp.toString())
            batch.set(docRef, data)
        }
    }


    override fun getUserHeartRateData(userId: String): Flow<List<BatimentoCardiaco>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("heart_rate")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val heartRateList = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(BatimentoCardiaco::class.java)
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Falha ao analisar BatimentoCardiaco: ${document.id}", e)
                    null
                }
            }
            emit(heartRateList)
        } else {
            emit(emptyList())
        }
    }

    override fun getAllHeartRateData(userId: String): Flow<List<BatimentoCardiaco>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("heart_rate")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val heartRateList = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(BatimentoCardiaco::class.java)
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Falha ao analisar BatimentoCardiaco (histórico): ${document.id}", e)
                    null
                }
            }
            emit(heartRateList)
        } else {
            emit(emptyList())
        }
    }

    /**
     * Busca o último registo de passos diários do utilizador.
     */
    override fun getUserStepsData(userId: String): Flow<List<Passos>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("steps")
                .orderBy("data", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val passosList = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Passos::class.java)
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Falha ao analisar Passos: ${document.id}", e)
                    null
                }
            }
            emit(passosList)
        } else {
            emit(emptyList())
        }
    }


    override fun getUserSleepData(userId: String): Flow<List<Sono>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("sleep")
                .orderBy("endTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val sleepList = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Sono::class.java)
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Falha ao analisar Sono: ${document.id}", e)
                    null
                }
            }
            emit(sleepList)
        } else {
            emit(emptyList())
        }
    }

    override fun getUserCaloriesData(userId: String): Flow<List<Calorias>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("calories")
                .whereEqualTo("tipo", "TOTAL")
                .orderBy("endTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val caloriesList = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Calorias::class.java)
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Falha ao analisar Calorias: ${document.id}", e)
                    null
                }
            }
            emit(caloriesList)
        } else {
            emit(emptyList())
        }
    }

}