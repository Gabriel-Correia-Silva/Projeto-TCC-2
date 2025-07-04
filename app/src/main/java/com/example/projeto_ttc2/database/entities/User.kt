package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // ID do Firebase
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val supervisorId: String? = null,
    val profileImageUrl: String? = null,
    val gender: String? = null,
    val birthDate: LocalDate? = null
)