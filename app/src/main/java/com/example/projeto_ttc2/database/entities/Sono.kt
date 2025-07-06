package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.Instant

@IgnoreExtraProperties
@Entity(tableName = "sono")
data class Sono(
    @PrimaryKey
    val healthConnectId: String = "",
    val startTime: Instant = Instant.EPOCH,
    val endTime: Instant = Instant.EPOCH,
    val durationMinutes: Long = 0L,
    val remSleepDurationMinutes: Long? = null,
    val deepSleepDurationMinutes: Long? = null,
    val lightSleepDurationMinutes: Long? = null,
    val awakeDurationMinutes: Long? = null,
    val userId: String = ""
)