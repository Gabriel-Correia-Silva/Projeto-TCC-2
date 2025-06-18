package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.projeto_ttc2.database.repository.AuthRepository
import com.example.projeto_ttc2.database.repository.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    public val authState: StateFlow<AuthState> = _authState

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole

    // Armazena o usuário durante o processo de registro
    var registrationUser: FirebaseUser? = null

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    val userDoc = repository.firestore.collection("users").document(user.uid).get().await()

                    if (userDoc.exists()) {
                        fetchUserRole(user.uid)
                    } else {
                        registrationUser = user
                        _authState.value = AuthState.NeedsRegistration(user)
                    }
                } else {
                    _authState.value = AuthState.Error("Usuário não encontrado após autenticação")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Falha na autenticação: ${e.message}")
            }
        }
    }

    private suspend fun fetchUserRole(userId: String) {
        try {
            val role = repository.getUserRole(userId)
            _userRole.value = when (role) {
                "supervisor" -> UserRole.Supervisor
                "supervised" -> UserRole.Supervised
                else -> UserRole.Unknown
            }
            _authState.value = AuthState.Authenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Falha ao obter perfil: ${e.message}")
        }
    }

    fun registerUser(
        user: FirebaseUser,
        name: String,
        role: String,
        supervisorId: String? = null
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.registerUser(
                    userId = user.uid,
                    name = name,
                    email = user.email ?: "",
                    role = role,
                    supervisorId = supervisorId
                )
                _userRole.value = if (role == "supervisor")
                    UserRole.Supervisor else UserRole.Supervised
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Falha no registro")
            }
        }
    }
    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun signOut() {
        Firebase.auth.signOut()
        _authState.value = AuthState.Idle
        _userRole.value = null
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
    data class NeedsRegistration(val user: FirebaseUser) : AuthState()
}

sealed class UserRole {
    object Supervisor : UserRole()
    object Supervised : UserRole()
    object Unknown : UserRole()
}