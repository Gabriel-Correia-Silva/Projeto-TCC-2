package com.example.projeto_ttc2.database.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.projeto_ttc2.database.entities.AccelerometerData
import com.example.projeto_ttc2.database.entities.GyroscopeData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val accelerometer: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val gyroscope: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun captureSensorData(durationMillis: Long): Flow<Pair<List<AccelerometerData>, List<GyroscopeData>>> = callbackFlow {
        val accelerometerReadings = mutableListOf<AccelerometerData>()
        val gyroscopeReadings = mutableListOf<GyroscopeData>()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                when (event?.sensor?.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelerometerReadings.add(
                            AccelerometerData(x = event.values[0], y = event.values[1], z = event.values[2])
                        )
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gyroscopeReadings.add(
                            GyroscopeData(x = event.values[0], y = event.values[1], z = event.values[2])
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)

        launch {
            delay(durationMillis)
            trySend(Pair(accelerometerReadings.toList(), gyroscopeReadings.toList()))
            close()
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}