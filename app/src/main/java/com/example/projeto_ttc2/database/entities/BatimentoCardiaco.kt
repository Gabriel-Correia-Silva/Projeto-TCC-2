package com.example.projeto_ttc2.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneOffset


@Entity(tableName = "batimentos_cardiacos")
data class BatimentoCardiaco(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "health_connect_id")
    val healthConnectId: String,

    @ColumnInfo(name = "bpm")
    val bpm: Long,

    @ColumnInfo(name = "timestamp")
    val timestamp: Instant,

    @ColumnInfo(name = "zone_offset")
    val zoneOffset: ZoneOffset?
)
