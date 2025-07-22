package com.example.projeto_ttc2.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.time.Instant
import java.util.Date

@IgnoreExtraProperties
@Entity(tableName = "sono")
data class Sono(
    @PrimaryKey
    val healthConnectId: String = "",
    @ServerTimestamp
    val startTime: Date? = null,
    @ServerTimestamp
    val endTime: Date? = null,
    val durationMinutes: Long = 0L,
    val remSleepDurationMinutes: Long? = null,
    val deepSleepDurationMinutes: Long? = null,
    val lightSleepDurationMinutes: Long? = null,
    val awakeDurationMinutes: Long? = null,
    val userId: String = ""
)

@Entity(
    tableName = "sleep_stages",
    foreignKeys = [
        ForeignKey(
            entity = Sono::class,
            parentColumns = ["healthConnectId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SleepStage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val type: Int,
    val startTime: Instant,
    val endTime: Instant
)

data class SonoWithStages(
    @Embedded val sono: Sono,
    @Relation(
        parentColumn = "healthConnectId",
        entityColumn = "sessionId"
    )
    val stages: List<SleepStage>
)