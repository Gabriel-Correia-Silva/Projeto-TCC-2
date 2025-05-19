package com.example.projeto_ttc2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batimento")
data class Batimento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bpm: Int,
    val timestamp: Long
)
