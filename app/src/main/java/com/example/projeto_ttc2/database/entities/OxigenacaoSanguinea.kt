package com.example.projeto_ttc2.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.Instant

@IgnoreExtraProperties
@Entity(tableName = "oxigenacao_sanguinea")
data class OxigenacaoSanguinea(
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = 0L,

    @ColumnInfo(name = "health_connect_id")
    val healthConnectId: String = "",

    @ColumnInfo(name = "spo2")
    val spo2: Double = 0.0,

    @ColumnInfo(name = "zone_offset")
    val zoneOffset: String? = null,

    val userId: String = ""
)