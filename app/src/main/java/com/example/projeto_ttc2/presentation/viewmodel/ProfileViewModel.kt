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
                _profileState.value = ProfileState.Error("Usuário não autenticado.")
                return@launch
            }

            try {
                val user = userRepository.getUser(currentUser.uid)
                if (user != null) {
                    _profileState.value = ProfileState.Success(user)
                } else {
                    _profileState.value = ProfileState.Error("Perfil de usuário não encontrado.")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Falha ao carregar perfil: ${e.message}")
            }
        }
    }

    fun saveProfile(
        fullName: String,
        gender: String,
        birthDate: LocalDate?,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _profileState.value = ProfileState.Error("Usuário não autenticado para salvar.")
                return@launch
            }

            try {
                val updates = mutableMapOf<String, Any?>()
                updates["name"] = fullName
                updates["gender"] = gender
                updates["birthDate"] = birthDate

                if (imageUri != null) {
                    val imageUrl = userRepository.uploadProfileImage(currentUser.uid, imageUri)
                    updates["profileImageUrl"] = imageUrl
                }

                userRepository.updateUser(currentUser.uid, updates)
                _profileState.value = ProfileState.UpdateSuccess
                // Recarrega os dados após a atualização bem-sucedida
                loadUserProfile()
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Falha ao salvar perfil: ${e.message}")
            }
        }
    }
}