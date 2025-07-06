package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val supervisorIds: List<String>? = null,
    val profileImageUrl: String? = null,
    val gender: String? = null,
    val birthDate: String? = null // Alterado de LocalDate? para String?
)