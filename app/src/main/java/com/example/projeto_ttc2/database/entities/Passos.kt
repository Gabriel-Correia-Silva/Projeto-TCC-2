package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "passos")
data class Passos(
    @PrimaryKey
    val data: LocalDate,
    val contagem: Long,
    val userId: String // Add this line
)