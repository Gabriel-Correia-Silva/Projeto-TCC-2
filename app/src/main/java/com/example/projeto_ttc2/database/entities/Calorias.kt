package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "calorias")
data class Calorias(
    @PrimaryKey
    val healthConnectId: String,
    val startTime: Instant,
    val endTime: Instant,
    val kilocalorias: Double,
    val tipo: String,
    val userId: String
)