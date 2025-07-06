package com.example.projeto_ttc2.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "batimentos_cardiacos")
data class BatimentoCardiaco(
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = 0L,

    @ColumnInfo(name = "health_connect_id")
    val healthConnectId: String = "",

    @ColumnInfo(name = "bpm")
    val bpm: Long = 0L,

    @ColumnInfo(name = "zone_offset")
    val zoneOffset: String? = null,

    val userId: String = ""
)