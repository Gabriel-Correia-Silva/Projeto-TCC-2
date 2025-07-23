package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "ble_accelerometer_readings")
data class BleAccelerometerReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val x: Float,
    val y: Float,
    val z: Float,
    val userId: String = "",
    val synced: Boolean = false
)