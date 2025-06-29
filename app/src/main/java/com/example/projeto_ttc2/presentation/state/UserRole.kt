package com.example.projeto_ttc2.presentation.state

sealed class UserRole {
    object Supervisor : UserRole()
    object Supervised : UserRole()
    object Unknown : UserRole()
}