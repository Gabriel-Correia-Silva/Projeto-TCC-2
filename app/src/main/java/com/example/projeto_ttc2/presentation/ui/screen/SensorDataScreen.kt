package com.example.projeto_ttc2.presentation.ui.screen

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Importar
import com.example.projeto_ttc2.background.SensorMonitoringService
import com.example.projeto_ttc2.database.entities.AccelerometerData
import com.example.projeto_ttc2.database.entities.GyroscopeData
import com.example.projeto_ttc2.presentation.viewmodel.SensorDataViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun SensorDataScreen(
    viewModel: SensorDataViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var phoneAccelerometerData by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var phoneGyroscopeData by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var isMonitoringInBackground by remember { mutableStateOf(false) }

    var isRecording by remember { mutableStateOf(false) }
    var recordedAccelerometerData by remember { mutableStateOf<List<AccelerometerData>>(emptyList()) }
    var recordedGyroscopeData by remember { mutableStateOf<List<GyroscopeData>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // Coleta dos dados do acelerômetro do anel BLE
    val latestBleAccelerometerData by viewModel.latestBleAccelerometerData.collectAsStateWithLifecycle()
    val latestBleHeartRate by viewModel.latestBleHeartRate.collectAsStateWithLifecycle()
    val latestBleSpo2 by viewModel.latestBleSpo2.collectAsStateWithLifecycle()


    // Listener para quando a UI está visível (sensores do telefone)
    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    when (it.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> phoneAccelerometerData = Triple(it.values[0], it.values[1], it.values[2])
                        Sensor.TYPE_GYROSCOPE -> phoneGyroscopeData = Triple(it.values[0], it.values[1], it.values[2])
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
        Text(
            text = "Dados dos Sensores",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Controles de monitoramento em segundo plano (sensores do telefone)
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

        // Exibição dos dados dos sensores do TELEFONE
        Text(
            text = "Sensores do Smartphone",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        SensorDisplayCard("Acelerômetro (Telefone)", phoneAccelerometerData)
        SensorDisplayCard("Giroscópio (Telefone)", phoneGyroscopeData)


        // Exibição dos dados dos sensores do ANEL COLMI
        Text(
            text = "Sensores do Anel Colmi R06 (via BLE)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        // Dados do Acelerômetro do Anel
        SensorDisplayCardRing(
            sensorName = "Acelerômetro (Anel)",
            data = latestBleAccelerometerData,
            suffix = " RAW",
            showGForce = true
        )
        // Dados de Frequência Cardíaca do Anel
        SensorDisplayCardRingBPM(
            sensorName = "Frequência Cardíaca (Anel)",
            bpm = latestBleHeartRate
        )
        // Dados de SpO2 do Anel
        SensorDisplayCardRingSpO2(
            sensorName = "Oxigenação Sanguínea (Anel)",
            spo2 = latestBleSpo2
        )
        // Você pode adicionar mais cards aqui para outros dados do anel (ex: PPG Bruto, Stress, Bateria)

        // Controles de Gravação e Salvamento (se aplicável aos dados do anel, atualmente para telefone)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    isRecording = true
                    recordedAccelerometerData = emptyList()
                    recordedGyroscopeData = emptyList()
                    Toast.makeText(context, "Gravação iniciada por 10 segundos...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch {
                        viewModel.sensorRepository.captureSensorData(10000L).collect { (accel, gyro) ->
                            recordedAccelerometerData = accel
                            recordedGyroscopeData = gyro
                            isRecording = false
                            Toast.makeText(context, "Gravação concluída!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isRecording
            ) {
                Text(if (isRecording) "Gravando..." else "Iniciar Gravação (Telefone)")
            }
            Button(
                onClick = {
                    saveSensorDataToCsv(
                        context,
                        recordedAccelerometerData,
                        "accelerometer",
                        "accelerometer_data_phone"
                    )
                    saveSensorDataToCsv(
                        context,
                        recordedGyroscopeData,
                        "gyroscope",
                        "gyroscope_data_phone"
                    )
                    recordedAccelerometerData = emptyList()
                    recordedGyroscopeData = emptyList()
                },
                enabled = !isRecording && (recordedAccelerometerData.isNotEmpty() || recordedGyroscopeData.isNotEmpty())
            ) {
                Text("Salvar Dados (Telefone)")
            }
        }
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
            Text("Monitorar Sensores (Telefone) em Segundo Plano", style = MaterialTheme.typography.bodyLarge)
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

// NOVO: Composable para exibir dados do acelerômetro do anel
@Composable
private fun SensorDisplayCardRing(
    sensorName: String,
    data: AccelerometerData,
    suffix: String = "",
    showGForce: Boolean = false
) {
    val x = data.x
    val y = data.y
    val z = data.z

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) // Usar uma cor diferente para diferenciar
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = sensorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Eixo X: %.2f%s".format(x, suffix), fontSize = 16.sp)
            Text(text = "Eixo Y: %.2f%s".format(y, suffix), fontSize = 16.sp)
            Text(text = "Eixo Z: %.2f%s".format(z, suffix), fontSize = 16.sp)

            if (showGForce) {
                // A conversão para G é feita na camada de ViewModel/Service antes de chegar aqui,
                // mas se os valores de 'x', 'y', 'z' aqui são os valores RAW, fazemos a conversão na UI para display.
                // Assumindo que 'x', 'y', 'z' em AccelerometerData já são os valores RAW
                val xG = x / 512.0f
                val yG = y / 512.0f
                val zG = z / 512.0f
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Força G (X): %.2fG".format(xG), fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(text = "Força G (Y): %.2fG".format(yG), fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(text = "Força G (Z): %.2fG".format(zG), fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

// NOVO: Composable para exibir dados de BPM do anel
@Composable
private fun SensorDisplayCardRingBPM(
    sensorName: String,
    bpm: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = sensorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "BPM: ${if (bpm > 0) bpm else "--"}", fontSize = 16.sp)
        }
    }
}

// NOVO: Composable para exibir dados de SpO2 do anel
@Composable
private fun SensorDisplayCardRingSpO2(
    sensorName: String,
    spo2: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = sensorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "SpO2: ${if (spo2 > 0) "%.1f".format(spo2) + "%" else "--"}", fontSize = 16.sp)
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

private fun saveSensorDataToCsv(
    context: Context,
    data: List<Any>,
    fileNamePrefix: String,
    directoryName: String
) {
    if (data.isEmpty()) {
        Toast.makeText(context, "Nenhum dado para salvar para $fileNamePrefix.", Toast.LENGTH_SHORT).show()
        return
    }

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val filename = "${fileNamePrefix}_${timestamp}.csv"

    val baseDir = context.getExternalFilesDir(null)
    val specificDir = File(baseDir, directoryName)
    if (!specificDir.exists()) {
        specificDir.mkdirs()
    }

    val file = File(specificDir, filename)

    try {
        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos).use { writer ->
                writer.append("timestamp,x,y,z\n")

                data.forEach { item ->
                    when (item) {
                        is AccelerometerData -> {
                            writer.append("${item.timestamp},${item.x},${item.y},${item.z}\n")
                        }
                        is GyroscopeData -> {
                            writer.append("${item.timestamp},${item.x},${item.y},${item.z}\n")
                        }
                        else -> {
                            // Caso de dados inesperados
                        }
                    }
                }
                writer.flush()
                Toast.makeText(context, "Dados salvos em: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                println("Data saved to: ${file.absolutePath}")
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao salvar dados CSV: ${e.message}", Toast.LENGTH_LONG).show()
        println("Error saving data to CSV: ${e.message}")
        e.printStackTrace()
    }
}