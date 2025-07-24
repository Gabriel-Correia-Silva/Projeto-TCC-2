package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.background.BleMonitoringService
import com.example.projeto_ttc2.database.repository.BleSensorPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleSensorSettingsViewModel @Inject constructor(
    private val bleSensorPreferencesRepository: BleSensorPreferencesRepository
) : ViewModel() {

    val accelerometerEnabled: StateFlow<Boolean> = bleSensorPreferencesRepository.accelerometerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val heartRateEnabled: StateFlow<Boolean> = bleSensorPreferencesRepository.heartRateEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val spo2Enabled: StateFlow<Boolean> = bleSensorPreferencesRepository.spo2Enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val stressEnabled: StateFlow<Boolean> = bleSensorPreferencesRepository.stressEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val stepsGeneralEnabled: StateFlow<Boolean> = bleSensorPreferencesRepository.stepsGeneralEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val heartRateInterval: StateFlow<Int> = bleSensorPreferencesRepository.heartRateInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val spo2Interval: StateFlow<Int> = bleSensorPreferencesRepository.spo2Interval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 600)

    fun setAccelerometerEnabled(enabled: Boolean) {
        bleSensorPreferencesRepository.setAccelerometerEnabled(enabled)
    }

    fun setHeartRateEnabled(enabled: Boolean) {
        bleSensorPreferencesRepository.setHeartRateEnabled(enabled)
    }

    fun setSpO2Enabled(enabled: Boolean) {
        bleSensorPreferencesRepository.setSpO2Enabled(enabled)
    }

    fun setStressEnabled(enabled: Boolean) {
        bleSensorPreferencesRepository.setStressEnabled(enabled)
    }

    fun setStepsGeneralEnabled(enabled: Boolean) {
        bleSensorPreferencesRepository.setStepsGeneralEnabled(enabled)
    }

    fun setHeartRateInterval(intervalInSeconds: Int) {
        bleSensorPreferencesRepository.setHeartRateInterval(intervalInSeconds)
    }

    fun setSpo2Interval(intervalInSeconds: Int) {
        bleSensorPreferencesRepository.setSpo2Interval(intervalInSeconds)
    }

    fun applySettingsAndRestartService(context: Context) {
        viewModelScope.launch {
            Toast.makeText(context, "A reiniciar o monitoramento com as novas configurações...", Toast.LENGTH_SHORT).show()

            val stopIntent = Intent(context, BleMonitoringService::class.java).apply {
                action = BleMonitoringService.ACTION_STOP_BLE_MONITORING
            }
            context.startService(stopIntent)

            delay(1000)

            val startIntent = Intent(context, BleMonitoringService::class.java).apply {
                action = BleMonitoringService.ACTION_START_BLE_MONITORING
            }
            context.startService(startIntent)

            Toast.makeText(context, "Monitoramento reiniciado!", Toast.LENGTH_SHORT).show()
        }
    }
}