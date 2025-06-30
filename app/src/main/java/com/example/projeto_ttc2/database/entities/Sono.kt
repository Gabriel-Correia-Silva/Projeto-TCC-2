package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "sono")
data class Sono(
    @PrimaryKey
    val healthConnectId: String,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Long,
    val remSleepDurationMinutes: Long? = null,
    val deepSleepDurationMinutes: Long? = null,
    val lightSleepDurationMinutes: Long? = null,
    val awakeDurationMinutes: Long? = null
)