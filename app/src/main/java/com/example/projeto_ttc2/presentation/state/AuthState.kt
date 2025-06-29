package com.example.projeto_ttc2.presentation.state

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
    data class NeedsRegistration(val user: FirebaseUser) : AuthState()
}