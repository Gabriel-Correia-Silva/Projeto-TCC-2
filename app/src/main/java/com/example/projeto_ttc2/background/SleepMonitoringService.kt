package com.example.projeto_ttc2.background

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.database.repository.SleepRepository
import com.example.projeto_ttc2.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SleepMonitoringService : LifecycleService() {

    @Inject
    lateinit var sleepRepository: SleepRepository

    private var monitoringJob: Job? = null

    companion object {
        const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "ACTION_STOP_MONITORING"
        private const val NOTIFICATION_ID = 3
        private const val CHANNEL_ID = "SleepMonitoringChannel"
        private const val SLEEP_THRESHOLD_MINUTES = 600L
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_MONITORING -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startMonitoring()
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        Log.d("SleepMonitoringService", "Iniciando monitoramento de sono.")
        monitoringJob = lifecycleScope.launch {
            while (true) {
                checkSleepDuration()
                delay(15 * 60 * 1000)
            }
        }
    }

    private suspend fun checkSleepDuration() {
        Log.d("SleepMonitoringService", "Verificando duração do sono...")
        val lastSleepSession = sleepRepository.getLatestSleepSession().firstOrNull() ?: return

        val durationMinutes = lastSleepSession.durationMinutes
        Log.d("SleepMonitoringService", "Duração atual: $durationMinutes min.")

        if (durationMinutes > SLEEP_THRESHOLD_MINUTES) {
            Log.w("SleepMonitoringService", "Sono excessivo detectado! Disparando alarme.")
            scheduleAlarm()
            stopMonitoring()
        }
    }

    private fun stopMonitoring() {
        Log.d("SleepMonitoringService", "Parando monitoramento de sono.")
        monitoringJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun scheduleAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, SleepAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        } else {

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Monitoramento de Sono", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoramento Noturno Ativo")
            .setContentText("Analisando sua sessão de sono.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}