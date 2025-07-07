package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.repository.AuthRepository
import com.example.projeto_ttc2.database.repository.AuthResult
import com.example.projeto_ttc2.presentation.state.AuthState
import com.example.projeto_ttc2.presentation.state.UserRole
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState


    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole

    fun getCurrentUser(): FirebaseUser? {
        return repository.getCurrentUser()
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val result = repository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    result.user?.uid?.let { fetchUserRole(it) }
                        ?: run { _authState.value = AuthState.Error("ID de usuário nulo.") }
                }
                is AuthResult.NeedsRegistration -> {
                    _authState.value = AuthState.NeedsRegistration(result.user)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    private suspend fun fetchUserRole(userId: String) {
        val role = repository.getUserRole(userId)
        _userRole.value = when (role) {
            "supervisor" -> UserRole.Supervisor
            "supervised" -> UserRole.Supervised
            else -> UserRole.Unknown
        }

        if (_userRole.value == UserRole.Unknown) {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                _authState.value = AuthState.NeedsRegistration(currentUser)
            } else {
                _authState.value = AuthState.Error("Usuário não encontrado")
            }
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    // Função para verificação de registro (usada pela SplashScreen)
    suspend fun checkUserRegistration(userId: String) {
        // Evita recarregar se já estiver autenticado com role válido
        if (_authState.value is AuthState.Authenticated && _userRole.value !in listOf(null, UserRole.Unknown)) {
            return
        }
        _authState.value = AuthState.Loading
        fetchUserRole(userId)
    }

    // Função de bloqueio para a lógica síncrona da nova SplashScreen
    suspend fun checkUserRegistrationBlocking(userId: String): AuthState {
        val role = repository.getUserRole(userId)
        _userRole.value = when (role) {
            "supervisor" -> UserRole.Supervisor
            "supervised" -> UserRole.Supervised
            else -> UserRole.Unknown
        }

        return if (_userRole.value == UserRole.Unknown) {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) AuthState.NeedsRegistration(currentUser) else AuthState.Error("Usuário não encontrado")
        } else {
            AuthState.Authenticated
        }
    }

    fun registerUser(user: FirebaseUser, name: String, role: String, supervisorIds: List<String>?) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.registerUser(user.uid, name, user.email ?: "", role, supervisorIds)
                fetchUserRole(user.uid)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Falha no registro")
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.Idle
        _userRole.value = null
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun clearErrorState() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}