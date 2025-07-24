package com.example.projeto_ttc2.presentation.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.background.BleConnectionManager
import com.example.projeto_ttc2.background.BleMonitoringService
import com.example.projeto_ttc2.background.MonitoringRestartReceiver
import com.example.projeto_ttc2.database.repository.BleSensorPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val bleConnectionManager: BleConnectionManager,
    private val preferencesRepository: BleSensorPreferencesRepository
) : ViewModel() {

    val isBleServiceRunning: StateFlow<Boolean> = bleConnectionManager.isServiceRunning

    val isMonitoringPaused: StateFlow<Boolean> = preferencesRepository.monitoringPausedUntil
        .map { it > System.currentTimeMillis() }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    fun toggleBleService(context: Context, enable: Boolean) {
        if (enable) {
            preferencesRepository.setMonitoringPausedUntil(0L) // Limpa a pausa
        }
        val intent = Intent(context, BleMonitoringService::class.java).apply {
            action = if (enable) {
                BleMonitoringService.ACTION_START_BLE_MONITORING
            } else {
                BleMonitoringService.ACTION_STOP_BLE_MONITORING
            }
        }
        context.startService(intent)
    }

    fun pauseMonitoring(context: Context, durationInMillis: Long) {
        val pauseUntilTimestamp = System.currentTimeMillis() + durationInMillis
        preferencesRepository.setMonitoringPausedUntil(pauseUntilTimestamp)

        // Para o serviço imediatamente
        val stopIntent = Intent(context, BleMonitoringService::class.java).apply {
            action = BleMonitoringService.ACTION_STOP_BLE_MONITORING
        }
        context.startService(stopIntent)

        // Agenda o reinício do serviço
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val restartIntent = Intent(context, MonitoringRestartReceiver::class.java).let {
            PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            pauseUntilTimestamp,
            restartIntent
        )
        Toast.makeText(context, "Monitoramento pausado.", Toast.LENGTH_SHORT).show()
    }
}