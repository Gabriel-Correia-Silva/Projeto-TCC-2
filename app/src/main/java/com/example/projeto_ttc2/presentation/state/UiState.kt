package com.example.projeto_ttc2.presentation.state

sealed interface UiState {
    data object Uninitialized : UiState
    data object Loading : UiState
    data object Success : UiState
    data class Error(val message: String) : UiState
    data object PermissionRequired : UiState
}