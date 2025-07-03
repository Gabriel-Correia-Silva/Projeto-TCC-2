package com.example.projeto_ttc2.database.repository

import android.net.Uri
import com.example.projeto_ttc2.database.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    override suspend fun createUser(user: User) {
        firestore.collection("users").document(user.uid).set(user).await()
    }

    override suspend fun getUser(userId: String): User? {
        return firestore.collection("users").document(userId).get().await()
            .toObject(User::class.java)
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
        firestore.collection("users").document(userId).update(updates).await()
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}