package com.example.projeto_ttc2.database.repository

import android.net.Uri
import android.util.Log
import com.example.projeto_ttc2.database.dao.UserDao
import com.example.projeto_ttc2.database.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun createUser(user: User) {
        usersCollection.document(user.id).set(user).await()
        userDao.upsert(user)
    }

    override suspend fun getUser(id: String): User? {
        var user = userDao.getById(id)
        if (user == null) {
            val document = usersCollection.document(id).get().await()
            if (document.exists()) {
                val firestoreUser = document.toObject(User::class.java)
                user = firestoreUser?.copy(id = document.id)
                user?.let { userDao.upsert(it) }
            }
        }
        return user
    }

    override suspend fun updateUser(id: String, updates: Map<String, Any?>) {
        usersCollection.document(id).update(updates).await()

        val currentUser = userDao.getById(id)
        if (currentUser != null) {
            val updatedUser = currentUser.copy(
                name = updates["name"] as? String ?: currentUser.name,
                gender = updates["gender"] as? String ?: currentUser.gender,
                birthDate = updates["birthDate"] as? String ?: currentUser.birthDate, // Espera String
                profileImageUrl = updates["profileImageUrl"] as? String ?: currentUser.profileImageUrl
            )
            userDao.update(updatedUser)
        }
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val storageRef = storage.reference.child("profile_images/$userId/${imageUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(imageUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    override fun getSupervisedUsers(supervisorId: String): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.whereArrayContains("supervisorIds", supervisorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { document ->
                        document.toObject(User::class.java)?.copy(id = document.id)
                    }
                    Log.d("Firestore", "Supervisionados carregados: ${users.size} usuários encontrados.")
                    trySend(users)
                }
            }
        awaitClose { listener.remove() }
    }
}