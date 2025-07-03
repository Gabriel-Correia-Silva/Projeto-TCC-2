package com.example.projeto_ttc2.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.repository.UserRepository
import com.example.projeto_ttc2.presentation.state.ProfileState
import com.example.projeto_ttc2.presentation.state.UiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState>(UiState.Uninitialized)
    val saveState: StateFlow<UiState> = _saveState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                _profileState.value = ProfileState.Error("Usuário não autenticado.")
                return@launch
            }
            try {
                val user = userRepository.getUser(userId)
                if (user != null) {
                    _profileState.value = ProfileState.Success(user)
                } else {
                    _profileState.value = ProfileState.Error("Perfil não encontrado.")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Falha ao carregar perfil.")
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
            _saveState.value = UiState.Loading
            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                _saveState.value = UiState.Error("Usuário não autenticado.")
                return@launch
            }

            try {
                var imageUrl: String? = null
                if (imageUri != null) {
                    imageUrl = userRepository.uploadProfileImage(userId, imageUri)
                }

                val updates = mutableMapOf<String, Any?>()
                updates["fullName"] = fullName
                updates["gender"] = gender
                updates["birthDate"] = birthDate?.toString()
                if (imageUrl != null) {
                    updates["profileImageUrl"] = imageUrl
                }

                userRepository.updateUser(userId, updates)
                _saveState.value = UiState.Success
                // Recarregar os dados após salvar
                loadUserProfile()
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Falha ao salvar o perfil.")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = UiState.Uninitialized
    }
}