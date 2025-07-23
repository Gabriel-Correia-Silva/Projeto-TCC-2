package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.presentation.viewmodel.BleSensorSettingsViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Monitoramento de Sensores do Anel Colmi",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            "Escolha quais dados do anel Colmi R06 deseja coletar em tempo real.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        SensorToggleItem(
            label = "Passos, Calorias e Distância (Geral)",
            description = "Ativa a coleta periódica de dados de atividade geral.",
            checked = stepsGeneralEnabled,
            onCheckedChange = { viewModel.setStepsGeneralEnabled(it, context) }
        )

        Divider()

        Text(
            "Dados de Sensores Brutos (Fluxo Contínuo):",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        SensorToggleItem(
            label = "Acelerômetro",
            description = "Ativa a coleta de dados brutos de movimento (X, Y, Z).",
            checked = accelerometerEnabled,
            onCheckedChange = { viewModel.setAccelerometerEnabled(it, context) }
        )

        Divider()

        Text(
            "Medições de Saúde (Solicitadas):",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        SensorToggleItem(
            label = "Frequência Cardíaca",
            description = "Solicita medições de batimento cardíaco.",
            checked = heartRateEnabled,
            onCheckedChange = { viewModel.setHeartRateEnabled(it, context) }
        )
        SensorToggleItem(
            label = "Oxigenação Sanguínea (SpO2)",
            description = "Solicita medições de saturação de oxigênio no sangue.",
            checked = spo2Enabled,
            onCheckedChange = { viewModel.setSpO2Enabled(it, context) }
        )
        SensorToggleItem(
            label = "Nível de Stress",
            description = "Solicita medições de nível de stress.",
            checked = stressEnabled,
            onCheckedChange = { viewModel.setStressEnabled(it, context) }
        )
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