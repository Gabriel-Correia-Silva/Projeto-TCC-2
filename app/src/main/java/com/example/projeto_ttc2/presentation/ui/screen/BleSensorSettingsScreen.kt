package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.presentation.viewmodel.BleSensorSettingsViewModel
import kotlin.math.roundToInt

@Composable
fun BleSensorSettingsScreen(
    viewModel: BleSensorSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val accelerometerEnabled by viewModel.accelerometerEnabled.collectAsStateWithLifecycle()
    val heartRateEnabled by viewModel.heartRateEnabled.collectAsStateWithLifecycle()
    val spo2Enabled by viewModel.spo2Enabled.collectAsStateWithLifecycle()
    val stressEnabled by viewModel.stressEnabled.collectAsStateWithLifecycle()
    val stepsGeneralEnabled by viewModel.stepsGeneralEnabled.collectAsStateWithLifecycle()

    val heartRateInterval by viewModel.heartRateInterval.collectAsStateWithLifecycle()
    val spo2Interval by viewModel.spo2Interval.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Monitoramento de Sensores do Anel",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Escolha quais dados do anel deseja coletar e com que frequência.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            SensorToggleItem(
                label = "Passos, Calorias e Distância (Geral)",
                description = "Ativa a coleta periódica de dados de atividade geral.",
                checked = stepsGeneralEnabled,
                onCheckedChange = { viewModel.setStepsGeneralEnabled(it) }
            )

            Divider()

            SensorToggleItem(
                label = "Acelerômetro (para quedas)",
                description = "Ativa a coleta de dados brutos de movimento.",
                checked = accelerometerEnabled,
                onCheckedChange = { viewModel.setAccelerometerEnabled(it) }
            )

            Divider()

            SensorToggleItem(
                label = "Frequência Cardíaca",
                description = "Ativa o monitoramento contínuo de batimentos.",
                checked = heartRateEnabled,
                onCheckedChange = { viewModel.setHeartRateEnabled(it) }
            )

            AnimatedVisibility(visible = heartRateEnabled) {
                IntervalSlider(
                    label = "Intervalo de Leitura (FC)",
                    value = heartRateInterval.toFloat(),
                    onValueChange = { viewModel.setHeartRateInterval(it.roundToInt()) },
                    range = 10f..60f,
                    steps = 4,
                    unit = "segundos"
                )
            }

            SensorToggleItem(
                label = "Oxigenação Sanguínea (SpO2)",
                description = "Ativa o monitoramento contínuo de SpO2.",
                checked = spo2Enabled,
                onCheckedChange = { viewModel.setSpO2Enabled(it) }
            )

            AnimatedVisibility(visible = spo2Enabled) {
                IntervalSlider(
                    label = "Intervalo de Leitura (SpO2)",
                    value = (spo2Interval / 60).toFloat(), // Convert seconds to minutes for slider
                    onValueChange = { viewModel.setSpo2Interval(it.roundToInt() * 60) }, // Convert minutes back to seconds
                    range = 1f..30f,
                    steps = 28,
                    unit = "minutos"
                )
            }
        }

        Button(
            onClick = { viewModel.applySettingsAndRestartService(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Salvar e Reiniciar Monitoramento")
        }
    }
}

@Composable
fun SensorToggleItem(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun IntervalSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    unit: String
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${sliderValue.roundToInt()} $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = range,
            steps = steps,
            onValueChangeFinished = { onValueChange(sliderValue) }
        )
    }
}