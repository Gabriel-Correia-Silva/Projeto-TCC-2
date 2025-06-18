package com.example.projeto_ttc2.database.local

data class DashboardData(
    val steps: Long = 0,
    val stepsGoal: Long = 8000,
    val distanceKm: Double = 0.0,
    val caloriesKcal: Double = 0.0,
    val activeCaloriesKcal: Double = 0.0,
    val heartRate: Long = 0,
    val sleepDurationMinutes: Long = 0
)