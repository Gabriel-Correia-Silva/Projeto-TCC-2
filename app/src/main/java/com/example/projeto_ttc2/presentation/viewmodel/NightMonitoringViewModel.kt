package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.projeto_ttc2.background.SleepMonitoringService
import com.example.projeto_ttc2.database.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NightMonitoringViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val isMonitoringEnabled: StateFlow<Boolean> = userPreferencesRepository.sleepMonitoringEnabled

    fun setMonitoringState(isEnabled: Boolean, context: Context) {
        userPreferencesRepository.setSleepMonitoringEnabled(isEnabled)
        if (isEnabled) {
            startMonitoringService(context)
        } else {
            stopMonitoringService(context)
        }
    }

    private fun startMonitoringService(context: Context) {
        val intent = Intent(context, SleepMonitoringService::class.java).apply {
            action = SleepMonitoringService.ACTION_START_MONITORING
        }
        context.startService(intent)
    }

    private fun stopMonitoringService(context: Context) {
        val intent = Intent(context, SleepMonitoringService::class.java).apply {
            action = SleepMonitoringService.ACTION_STOP_MONITORING
        }
        context.startService(intent)
    }
}