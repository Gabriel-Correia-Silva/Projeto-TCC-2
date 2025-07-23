package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.background.BleMonitoringService
import com.example.projeto_ttc2.database.repository.BleSensorPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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


    fun setAccelerometerEnabled(enabled: Boolean, context: Context) {
        bleSensorPreferencesRepository.setAccelerometerEnabled(enabled)
        restartBleMonitoringService(context)
    }

    fun setHeartRateEnabled(enabled: Boolean, context: Context) {
        bleSensorPreferencesRepository.setHeartRateEnabled(enabled)
        restartBleMonitoringService(context)
    }

    fun setSpO2Enabled(enabled: Boolean, context: Context) {
        bleSensorPreferencesRepository.setSpO2Enabled(enabled)
        restartBleMonitoringService(context)
    }

    fun setStressEnabled(enabled: Boolean, context: Context) {
        bleSensorPreferencesRepository.setStressEnabled(enabled)
        restartBleMonitoringService(context)
    }

    fun setStepsGeneralEnabled(enabled: Boolean, context: Context) {
        bleSensorPreferencesRepository.setStepsGeneralEnabled(enabled)
        restartBleMonitoringService(context)
    }

    private fun restartBleMonitoringService(context: Context) {
        val stopIntent = Intent(context, BleMonitoringService::class.java).apply {
            action = BleMonitoringService.ACTION_STOP_BLE_MONITORING
        }
        context.startService(stopIntent)

        val startIntent = Intent(context, BleMonitoringService::class.java).apply {
            action = BleMonitoringService.ACTION_START_BLE_MONITORING
        }

        context.startService(startIntent)
    }
}