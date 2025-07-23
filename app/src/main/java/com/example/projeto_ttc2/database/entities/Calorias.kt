package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.time.Instant

@IgnoreExtraProperties
@Entity(tableName = "calorias")
data class Calorias(
    @PrimaryKey
    val healthConnectId: String = "",
    @ServerTimestamp
    val startTime: Instant? = null,
    @ServerTimestamp
    val endTime: Instant? = null,
    val kilocalorias: Double = 0.0,
    val tipo: String = "",
    val userId: String = ""
)