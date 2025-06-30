package com.example.projeto_ttc2.database.local

import com.example.projeto_ttc2.database.entities.Sono

data class DashboardData(
    val heartRate: Long = 0,
    val steps: Long = 0,
    val stepsGoal: Long = 10000,
    val distanceKm: Double = 0.0,
    val caloriesKcal: Double = 2000.0,
    val activeCaloriesKcal: Double = 0.0,
    val sleepSession: Sono? = null
)