package com.example.projeto_ttc2.database.repository

import com.example.projeto_ttc2.database.entities.AccelerometerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleSensorDataRepository @Inject constructor() {


    private val _latestAccelerometerData = MutableStateFlow(AccelerometerData(x = 0f, y = 0f, z = 0f))
    val latestAccelerometerData: StateFlow<AccelerometerData> = _latestAccelerometerData

    private val _latestHeartRate = MutableStateFlow(0L)
    val latestHeartRate: StateFlow<Long> = _latestHeartRate

    private val _latestSpo2 = MutableStateFlow(0.0)
    val latestSpo2: StateFlow<Double> = _latestSpo2

    fun updateAccelerometerData(x: Float, y: Float, z: Float) {
        _latestAccelerometerData.value = AccelerometerData(x = x, y = y, z = z)
    }

    fun updateHeartRate(bpm: Long) {
        _latestHeartRate.value = bpm
    }

    fun updateSpo2(spo2: Double) {
        _latestSpo2.value = spo2
    }


}