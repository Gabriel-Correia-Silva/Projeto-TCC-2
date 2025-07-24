package com.example.projeto_ttc2.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.projeto_ttc2.database.repository.BleSensorPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringRestartReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: BleSensorPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        preferencesRepository.setMonitoringPausedUntil(0L)

        val serviceIntent = Intent(context, BleMonitoringService::class.java).apply {
            action = BleMonitoringService.ACTION_START_BLE_MONITORING
        }
        context.startService(serviceIntent)
    }
}