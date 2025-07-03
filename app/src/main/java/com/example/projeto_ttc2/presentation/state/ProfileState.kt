package com.example.projeto_ttc2.presentation.state

import com.example.projeto_ttc2.database.entities.User

sealed interface ProfileState {
    data object Initial : ProfileState
    data object Loading : ProfileState
    data class Success(val user: User) : ProfileState
    data class Error(val message: String) : ProfileState
    data object UpdateSuccess : ProfileState
}