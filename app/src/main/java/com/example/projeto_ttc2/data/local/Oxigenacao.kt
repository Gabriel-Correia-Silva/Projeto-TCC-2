package com.example.projeto_ttc2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oxigenacao")
data class Oxigenacao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val percentual: Float,
    val timestamp: Long
)
