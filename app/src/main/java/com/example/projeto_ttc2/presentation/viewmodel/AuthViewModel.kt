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

    init {
        // Não chama checkCurrentUser aqui para evitar loops
        // A verificação será feita apenas quando necessário
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                _authState.value = AuthState.Loading
                fetchUserRole(currentUser.uid)
            }
        }
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
        try {
            val role = repository.getUserRole(userId)
            _userRole.value = when (role) {
                "supervisor" -> UserRole.Supervisor
                "supervised" -> UserRole.Supervised
                else -> UserRole.Unknown
            }

            // Se o role for Unknown, isso significa que o usuário não completou o registro
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
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Erro ao verificar role do usuário: ${e.message}")
        }
    }

    fun registerUser(user: FirebaseUser, name: String, role: String, supervisorId: String?) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.registerUser(user.uid, name, user.email ?: "", role, supervisorId)
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

    // Adicionando métodos que estão sendo referenciados no Navigation.kt
    suspend fun checkUserRegistration(userId: String) {
        // Evita recarregar se já estiver autenticado com role válido
        if (_authState.value is AuthState.Authenticated && _userRole.value != UserRole.Unknown) {
            return
        }

        _authState.value = AuthState.Loading
        fetchUserRole(userId)
    }

    fun completeRegistration(userId: String, role: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val currentUser = repository.getCurrentUser()
                if (currentUser != null) {
                    repository.registerUser(
                        userId = userId,
                        name = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        role = role,
                        supervisorId = null
                    )
                    fetchUserRole(userId)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Falha no registro")
            }
        }
    }
}