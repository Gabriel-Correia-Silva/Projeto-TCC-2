package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.Instant

@IgnoreExtraProperties
@Entity(tableName = "calorias")
data class Calorias(
    @PrimaryKey
    val healthConnectId: String = "",
    val startTime: Instant = Instant.EPOCH,
    val endTime: Instant = Instant.EPOCH,
    val kilocalorias: Double = 0.0,
    val tipo: String = "",
    val userId: String = ""
)