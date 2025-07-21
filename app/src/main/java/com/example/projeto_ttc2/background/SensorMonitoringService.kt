package com.example.projeto_ttc2.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.presentation.MainActivity

class SensorMonitoringService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    companion object {
        const val ACTION_START = "ACTION_START_SENSOR_MONITORING"
        const val ACTION_STOP = "ACTION_STOP_SENSOR_MONITORING"
        private const val NOTIFICATION_ID = 4
        private const val CHANNEL_ID = "SensorMonitoringChannel"
        private const val TAG = "SensorMonitoringService"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        Log.d(TAG, "Iniciando o monitoramento de sensores em segundo plano.")
        startForeground(NOTIFICATION_ID, createNotification().build())
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopMonitoring() {
        Log.d(TAG, "Parando o monitoramento de sensores.")
        sensorManager.unregisterListener(this)
        stopForeground(true)
        stopSelf()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    Log.d(TAG, "Acelerômetro (BG) -> X: ${it.values[0]}, Y: ${it.values[1]}, Z: ${it.values[2]}")
                }
                Sensor.TYPE_GYROSCOPE -> {
                    Log.d(TAG, "Giroscópio (BG) -> X: ${it.values[0]}, Y: ${it.values[1]}, Z: ${it.values[2]}")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createNotification(): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitoramento de Sensores",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoramento Ativo")
            .setContentText("Coletando dados dos sensores.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}