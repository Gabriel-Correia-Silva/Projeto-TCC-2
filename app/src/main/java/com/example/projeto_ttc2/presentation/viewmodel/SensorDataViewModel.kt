package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.AccelerometerData
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


    val latestBleAccelerometerData: StateFlow<AccelerometerData> = bleSensorDataRepository.latestAccelerometerData
        .onEach { data ->
            Log.d("SensorDataViewModel", "latestBleAccelerometerData atualizado: X=${data.x}, Y=${data.y}, Z=${data.z}")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccelerometerData(x = 0f, y = 0f, z = 0f))

    val latestBleHeartRate: StateFlow<Long> = bleSensorDataRepository.latestHeartRate
        .onEach { bpm ->
            Log.d("SensorDataViewModel", "latestBleHeartRate atualizado: BPM=${bpm}")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val latestBleSpo2: StateFlow<Double> = bleSensorDataRepository.latestSpo2
        .onEach { spo2 ->
            Log.d("SensorDataViewModel", "latestBleSpo2 atualizado: SpO2=${spo2}")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}