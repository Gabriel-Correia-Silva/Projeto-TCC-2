package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.RingAccelerometerData
import com.example.projeto_ttc2.database.repository.BleSensorDataRepository
import com.example.projeto_ttc2.database.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class SensorDataViewModel @Inject constructor(
    val sensorRepository: SensorRepository,
    private val bleSensorDataRepository: BleSensorDataRepository
) : ViewModel() {

    val latestBleAccelerometerData: StateFlow<RingAccelerometerData> = bleSensorDataRepository.newRingAccelerometerReading
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RingAccelerometerData(x = 0f, y = 0f, z = 0f)
        )

    val latestBleHeartRate: StateFlow<Long> = bleSensorDataRepository.latestHeartRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val latestBleSpo2: StateFlow<Double> = bleSensorDataRepository.latestSpo2
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val latestBleStress: StateFlow<Int> = bleSensorDataRepository.latestStress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val latestBleBattery: StateFlow<Pair<Int, Boolean>> = bleSensorDataRepository.latestBattery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, false))
}