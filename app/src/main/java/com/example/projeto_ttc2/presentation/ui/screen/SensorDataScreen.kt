package com.example.projeto_ttc2.presentation.ui.screen

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.background.SensorMonitoringService

@Composable
fun SensorDataScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var accelerometerData by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var gyroscopeData by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var isMonitoringInBackground by remember { mutableStateOf(false) }

    // Listener para quando a UI está visível
    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    when (it.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> accelerometerData = Triple(it.values[0], it.values[1], it.values[2])
                        Sensor.TYPE_GYROSCOPE -> gyroscopeData = Triple(it.values[0], it.values[1], it.values[2])
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorListener, gyroscope, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackgroundMonitoringControl(
            isMonitoring = isMonitoringInBackground,
            onToggle = {
                isMonitoringInBackground = it
                if (it) {
                    startSensorService(context)
                } else {
                    stopSensorService(context)
                }
            }
        )
        SensorDisplayCard("Acelerômetro (Tempo Real)", accelerometerData)
        SensorDisplayCard("Giroscópio (Tempo Real)", gyroscopeData)
    }
}

@Composable
private fun BackgroundMonitoringControl(isMonitoring: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Monitorar em Segundo Plano", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isMonitoring,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun SensorDisplayCard(sensorName: String, data: Triple<Float, Float, Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = sensorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Eixo X: %.2f".format(data.first), fontSize = 16.sp)
            Text(text = "Eixo Y: %.2f".format(data.second), fontSize = 16.sp)
            Text(text = "Eixo Z: %.2f".format(data.third), fontSize = 16.sp)
        }
    }
}

private fun startSensorService(context: Context) {
    Intent(context, SensorMonitoringService::class.java).also { intent ->
        intent.action = SensorMonitoringService.ACTION_START
        context.startService(intent)
    }
}

private fun stopSensorService(context: Context) {
    Intent(context, SensorMonitoringService::class.java).also { intent ->
        intent.action = SensorMonitoringService.ACTION_STOP
        context.startService(intent)
    }
}