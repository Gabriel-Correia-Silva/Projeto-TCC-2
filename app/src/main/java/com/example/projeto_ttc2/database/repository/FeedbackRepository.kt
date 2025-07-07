package com.example.projeto_ttc2.database.repository

import com.example.projeto_ttc2.database.entities.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val feedbackCollection = firestore.collection("feedback")

    suspend fun sendFeedback(feedback: Feedback) {
        feedbackCollection.add(feedback).await()
    }

    fun getFeedbackForUser(userId: String): Flow<List<Feedback>> = callbackFlow {
        val listener = feedbackCollection
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val feedbackList = snapshot?.documents?.mapNotNull {
                    it.toObject(Feedback::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(feedbackList)
            }
        awaitClose { listener.remove() }
    }

    fun getUnreadFeedbackCount(userId: String): Flow<Int> = callbackFlow {
        val listener = feedbackCollection
            .whereEqualTo("recipientId", userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markFeedbackAsRead(feedbackIds: List<String>) {
        val batch = firestore.batch()
        feedbackIds.forEach { id ->
            val docRef = feedbackCollection.document(id)
            batch.update(docRef, "read", true)
        }
        batch.commit().await()
    }
}