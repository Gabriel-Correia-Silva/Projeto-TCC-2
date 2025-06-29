package com.example.projeto_ttc2.presentation.state

sealed class UiState {
    object Uninitialized : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}