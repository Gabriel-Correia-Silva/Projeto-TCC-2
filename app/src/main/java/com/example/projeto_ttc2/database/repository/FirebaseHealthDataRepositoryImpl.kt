package com.example.projeto_ttc2.database.repository

import android.util.Log
import com.example.projeto_ttc2.database.entities.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseHealthDataRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirebaseHealthDataRepository {

    override suspend fun syncHeartRateData(userId: String, heartRateData: List<BatimentoCardiaco>) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("heart_rate")
        val batch = firestore.batch()
        for (data in heartRateData) {
            val docRef = collection.document(data.timestamp.toString())
            batch.set(docRef, data)
        }
        batch.commit().await()
    }

    override suspend fun syncStepsData(userId: String, stepsData: List<Passos>) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("steps")
        val batch = firestore.batch()
        for (data in stepsData) {
            val docRef = collection.document(data.data)
            batch.set(docRef, data)
        }
        batch.commit().await()
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
    }

    override suspend fun syncOxygenSaturationData(userId: String, oxygenData: List<OxigenacaoSanguinea>) {
        if (userId.isBlank()) return
        val collection = firestore.collection("users").document(userId).collection("oxygen_saturation")
        val batch = firestore.batch()
        for (data in oxygenData) {
            val docRef = collection.document(data.timestamp.toString())
            batch.set(docRef, data)
        }
        batch.commit().await()
    }

    private fun getTimestampFromMap(map: Map<String, Any>?): Timestamp? {
        if (map == null) return null
        val seconds = map["epochSecond"] as? Long ?: return null
        val nanos = (map["nano"] as? Long)?.toInt() ?: 0
        return Timestamp(seconds, nanos)
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
                    BatimentoCardiaco(
                        timestamp = document.getLong("timestamp") ?: 0L,
                        healthConnectId = document.getString("healthConnectId") ?: "",
                        bpm = document.getLong("bpm") ?: 0L,
                        zoneOffset = document.getString("zoneOffset"),
                        userId = document.getString("userId") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Falha ao analisar Batimento: ${document.id}", e)
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
                    BatimentoCardiaco(
                        timestamp = document.getLong("timestamp") ?: 0L,
                        healthConnectId = document.getString("healthConnectId") ?: "",
                        bpm = document.getLong("bpm") ?: 0L,
                        zoneOffset = document.getString("zoneOffset"),
                        userId = document.getString("userId") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Falha ao analisar Batimento (histórico): ${document.id}", e)
                    null
                }
            }
            emit(heartRateList)
        } else {
            emit(emptyList())
        }
    }

    override fun getUserStepsData(userId: String): Flow<List<Passos>> = flow {
        if (userId.isNotBlank()) {
            val snapshot = firestore.collection("users").document(userId).collection("steps")
                .orderBy("data", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val passosList = snapshot.documents.mapNotNull { document ->
                try {
                    val contagem = document.getLong("contagem") ?: 0L
                    val docUserId = document.getString("userId") ?: ""
                    val dataString: String? = when (val dataField = document.get("data")) {
                        is String -> dataField
                        is Map<*, *> -> {
                            val map = dataField as Map<String, Any>
                            val year = map["year"] as? Long
                            val month = map["monthValue"] as? Long
                            val day = map["dayOfMonth"] as? Long
                            if (year != null && month != null && day != null) {
                                String.format("%d-%02d-%02d", year, month, day)
                            } else null
                        }
                        else -> null
                    }
                    if (dataString != null) {
                        Passos(data = dataString, contagem = contagem, userId = docUserId)
                    } else null
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
                    val startTimeMap = document.get("startTime") as? Map<String, Any>
                    val endTimeMap = document.get("endTime") as? Map<String, Any>
                    val startTime = getTimestampFromMap(startTimeMap)?.toDate()?.toInstant() ?: Instant.EPOCH
                    val endTime = getTimestampFromMap(endTimeMap)?.toDate()?.toInstant() ?: Instant.EPOCH
                    Sono(
                        healthConnectId = document.getString("healthConnectId") ?: "",
                        startTime = startTime,
                        endTime = endTime,
                        durationMinutes = document.getLong("durationMinutes") ?: 0L,
                        remSleepDurationMinutes = document.getLong("remSleepDurationMinutes"),
                        deepSleepDurationMinutes = document.getLong("deepSleepDurationMinutes"),
                        lightSleepDurationMinutes = document.getLong("lightSleepDurationMinutes"),
                        awakeDurationMinutes = document.getLong("awakeDurationMinutes"),
                        userId = document.getString("userId") ?: ""
                    )
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
                    val startTimeMap = document.get("startTime") as? Map<String, Any>
                    val endTimeMap = document.get("endTime") as? Map<String, Any>
                    val startTime = getTimestampFromMap(startTimeMap)?.toDate()?.toInstant() ?: Instant.EPOCH
                    val endTime = getTimestampFromMap(endTimeMap)?.toDate()?.toInstant() ?: Instant.EPOCH
                    Calorias(
                        healthConnectId = document.getString("healthConnectId") ?: "",
                        startTime = startTime,
                        endTime = endTime,
                        kilocalorias = document.getDouble("kilocalorias") ?: 0.0,
                        tipo = document.getString("tipo") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
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