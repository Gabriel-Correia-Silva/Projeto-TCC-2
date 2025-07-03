package com.example.projeto_ttc2.database.entities

import java.time.LocalDate

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "",
    val supervisorId: String? = null,
    val birthDate: LocalDate? = null,
    val gender: String? = null,
    val profileImageUrl: String? = null
)