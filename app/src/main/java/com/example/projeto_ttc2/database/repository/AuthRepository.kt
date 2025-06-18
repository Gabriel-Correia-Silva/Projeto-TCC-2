package com.example.projeto_ttc2.database.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    val firestore: FirebaseFirestore
) {
    private val auth: FirebaseAuth = Firebase.auth

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                // Verifica se o usuário já está registrado
                val userDoc = firestore.collection("users").document(user.uid).get().await()

                if (!userDoc.exists()) {
                    // Primeiro login - redirecionar para registro
                    return AuthResult.NeedsRegistration(user)
                }
            }

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Falha na autenticação")
        }
    }

    suspend fun registerUser(
        userId: String,
        name: String,
        email: String,
        role: String,
        supervisorId: String? = null
    ) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to role,
            "supervisorId" to supervisorId
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .await()
    }

    suspend fun getUserRole(userId: String): String {
        val document = firestore.collection("users").document(userId).get().await()
        return document.getString("role") ?: "unknown"
    }

    fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}

sealed class AuthResult {
    data class Success(val user: FirebaseUser?) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class NeedsRegistration(val user: FirebaseUser) : AuthResult()
}