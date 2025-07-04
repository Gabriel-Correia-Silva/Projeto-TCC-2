package com.example.projeto_ttc2.database.repository

import android.net.Uri
import com.example.projeto_ttc2.database.dao.UserDao
import com.example.projeto_ttc2.database.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
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
        // Salva no Firestore
        usersCollection.document(user.id).set(user).await()
        // Salva no Room
        userDao.upsert(user)
    }

    override suspend fun getUser(id: String): User? {
        // Tenta obter do Room primeiro para acesso rápido
        var user = userDao.getById(id)
        if (user == null) {
            // Se não estiver no Room, busca no Firestore
            val document = usersCollection.document(id).get().await()
            if (document.exists()) {
                user = document.toObject(User::class.java)
                // Salva no Room para cache futuro
                user?.let { userDao.upsert(it) }
            }
        }
        return user
    }

    override suspend fun updateUser(id: String, updates: Map<String, Any?>) {
        // Atualiza no Firestore
        usersCollection.document(id).update(updates).await()

        // Atualiza no Room
        val currentUser = userDao.getById(id)
        if (currentUser != null) {
            // Cria uma cópia atualizada do usuário. Isso é simplificado.
            // Uma abordagem mais robusta lidaria com cada campo individualmente.
            val updatedUser = currentUser.copy(
                name = updates["name"] as? String ?: currentUser.name,
                gender = updates["gender"] as? String ?: currentUser.gender,
                birthDate = updates["birthDate"] as? LocalDate ?: currentUser.birthDate,
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
}