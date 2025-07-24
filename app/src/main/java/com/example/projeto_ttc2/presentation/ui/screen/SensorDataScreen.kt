package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.database.entities.RingAccelerometerData
import com.example.projeto_ttc2.presentation.viewmodel.SensorDataViewModel

@Composable
fun SensorDataScreen(
    viewModel: SensorDataViewModel = hiltViewModel()
) {
    val latestBleAccelerometerData by viewModel.latestBleAccelerometerData.collectAsStateWithLifecycle()
    val latestBleHeartRate by viewModel.latestBleHeartRate.collectAsStateWithLifecycle()
    val latestBleSpo2 by viewModel.latestBleSpo2.collectAsStateWithLifecycle()
    val latestBleStress by viewModel.latestBleStress.collectAsStateWithLifecycle()
    val latestBleBattery by viewModel.latestBleBattery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dados dos Sensores do Anel",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SensorDisplayCardRing(
            sensorName = "Acelerômetro",
            data = latestBleAccelerometerData,
            suffix = " m/s²",
            showGForce = true
        )

        SensorDisplayCardRingBPM(
            sensorName = "Frequência Cardíaca",
            bpm = latestBleHeartRate
        )

        SensorDisplayCardRingSpO2(
            sensorName = "Oxigenação Sanguínea",
            spo2 = latestBleSpo2
        )

        SensorDisplayCardRingStress(
            sensorName = "Nível de Stress",
            stress = latestBleStress
        )

        SensorDisplayCardRingBattery(
            sensorName = "Bateria do Anel",
            batteryLevel = latestBleBattery.first,
            isCharging = latestBleBattery.second
        )
    }
}

@Composable
private fun SensorDisplayCardRing(
    sensorName: String,
    data: RingAccelerometerData,
    suffix: String = "",
    showGForce: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = sensorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Eixo X: %.2f%s".format(data.x, suffix), fontSize = 16.sp)
            Text(text = "Eixo Y: %.2f%s".format(data.y, suffix), fontSize = 16.sp)
            Text(text = "Eixo Z: %.2f%s".format(data.z, suffix), fontSize = 16.sp)

            if (showGForce) {
                val xG = data.x / 9.80665f
                val yG = data.y / 9.80665f
                val zG = data.z / 9.80665f
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Força G (X): %.2fG".format(xG), fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(text = "Força G (Y): %.2fG".format(yG), fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(text = "Força G (Z): %.2fG".format(zG), fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun SensorDisplayCardRingBPM(
    sensorName: String,
    bpm: Long
) {
    GenericSensorCard(
        sensorName = sensorName,
        value = if (bpm > 0) bpm.toString() else "--",
        unit = "bpm",
        icon = Icons.Default.Favorite
    )
}

@Composable
private fun SensorDisplayCardRingSpO2(
    sensorName: String,
    spo2: Double
) {
    GenericSensorCard(
        sensorName = sensorName,
        value = if (spo2 > 0) "%.1f".format(spo2) else "--",
        unit = "%",
        icon = Icons.Default.Favorite // Placeholder, consider a better icon
    )
}

@Composable
private fun SensorDisplayCardRingStress(
    sensorName: String,
    stress: Int
) {
    GenericSensorCard(
        sensorName = sensorName,
        value = if (stress > 0) stress.toString() else "--",
        unit = "",
        icon = Icons.Default.Spa
    )
}

@Composable
private fun SensorDisplayCardRingBattery(
    sensorName: String,
    batteryLevel: Int,
    isCharging: Boolean
) {
    val statusText = if (isCharging) "A carregar" else "Nível"
    GenericSensorCard(
        sensorName = sensorName,
        value = if (batteryLevel > 0) "$batteryLevel%" else "--",
        unit = statusText,
        icon = Icons.Default.BatteryChargingFull
    )
}

@Composable
private fun GenericSensorCard(
    sensorName: String,
    value: String,
    unit: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = sensorName,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sensorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = value, style = MaterialTheme.typography.headlineMedium)
                    if (unit.isNotBlank()) {
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}