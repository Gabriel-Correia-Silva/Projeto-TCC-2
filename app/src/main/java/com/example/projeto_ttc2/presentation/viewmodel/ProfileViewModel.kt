package com.example.projeto_ttc2.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.repository.AuthRepository
import com.example.projeto_ttc2.database.repository.UserRepository
import com.example.projeto_ttc2.presentation.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _profileState.value = ProfileState.Error("Utilizador não autenticado.")
                return@launch
            }

            try {
                val user = userRepository.getUser(currentUser.uid)
                if (user != null) {
                    _profileState.value = ProfileState.Success(user)
                } else {
                    _profileState.value = ProfileState.Error("Perfil de utilizador não encontrado.")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Falha ao carregar perfil: ${e.message}")
            }
        }
    }

    fun clearState() {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            _profileState.value = ProfileState.Success(currentState.user)
        } else {
            _profileState.value = ProfileState.Initial
        }
    }

    // --- FUNÇÃO MODIFICADA ---
    fun saveProfile(
        fullName: String,
        birthDate: LocalDate?
    ) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _profileState.value = ProfileState.Error("Utilizador não autenticado para guardar.")
                return@launch
            }

            try {
                // Apenas nome e data de nascimento são atualizados
                val updates = mutableMapOf<String, Any?>()
                updates["name"] = fullName
                updates["birthDate"] = birthDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)

                userRepository.updateUser(currentUser.uid, updates)
                _profileState.value = ProfileState.UpdateSuccess
                loadUserProfile()
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Falha ao salvar perfil: ${e.message}")
            }
        }
    }
}