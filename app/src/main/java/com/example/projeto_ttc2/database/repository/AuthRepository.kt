package com.example.projeto_ttc2.database.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {

                val userDoc = firestore.collection("users").document(user.uid).get().await()
                if (userDoc.exists()) {

                    AuthResult.Success(user)
                } else {

                    AuthResult.NeedsRegistration(user)
                }
            } else {

                AuthResult.Error("Usuário nulo após autenticação.")
            }
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

        firestore.collection("users").document(userId).set(userData).await()
    }

    suspend fun getUserRole(userId: String): String {
        return try {

            firestore.collection("users").document(userId).get().await()
                .getString("role") ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }


    fun signOut() {
        auth.signOut()
    }
}

sealed class AuthResult {
    data class Success(val user: FirebaseUser?) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class NeedsRegistration(val user: FirebaseUser) : AuthResult()
}