package com.example.projeto_ttc2.database.entities

data class GyroscopeData(
    val timestamp: Long = System.currentTimeMillis(),
    val x: Float,
    val y: Float,
    val z: Float
)