package com.example.projeto_ttc2.database.repository

import android.net.Uri
import com.example.projeto_ttc2.database.entities.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUser(user: User)
    suspend fun getUser(userId: String): User?
    suspend fun updateUser(userId: String, updates: Map<String, Any?>)
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String
    fun getSupervisedUsers(supervisorId: String): Flow<List<User>>
}