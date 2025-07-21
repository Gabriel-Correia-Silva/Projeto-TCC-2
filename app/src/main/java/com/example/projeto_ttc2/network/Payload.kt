package com.example.projeto_ttc2.network

import com.example.projeto_ttc2.database.entities.*


data class SleepSessionPayload(
    val sessionSummary: Sono,
    val stages: List<SleepStage>
)
data class HourlyStepsPayload(
    val date: String,
    val hourlyCounts: Map<Int, Long>
)
data class DetailedHealthAndSensorPayload(
    val userId: String,
    val timestamp: Long,
    val heartRateRecords: List<BatimentoCardiaco>,
    val steps: HourlyStepsPayload,
    val sleepSessions: List<SleepSessionPayload>,
    val calorieRecords: List<Calorias>,
    val oxygenSaturationRecords: List<OxigenacaoSanguinea>,
    val accelerometerReadings: List<AccelerometerData>,
    val gyroscopeReadings: List<GyroscopeData>
)