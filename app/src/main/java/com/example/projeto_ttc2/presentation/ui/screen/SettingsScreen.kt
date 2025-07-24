package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.projeto_ttc2.presentation.state.UserRole
import com.example.projeto_ttc2.presentation.viewmodel.SettingsViewModel
import java.util.concurrent.TimeUnit

val TealColor = Color(0xFF4DB6AC)

@Composable
fun SettingsScreen(
    navController: NavController,
    userRole: UserRole?,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isBleServiceRunning by viewModel.isBleServiceRunning.collectAsStateWithLifecycle()
    val isMonitoringPaused by viewModel.isMonitoringPaused.collectAsStateWithLifecycle()
    var showPauseDialog by remember { mutableStateOf(false) }

    if (showPauseDialog) {
        PauseDialog(
            onDismiss = { showPauseDialog = false },
            onConfirm = { durationMinutes ->
                viewModel.pauseMonitoring(context, TimeUnit.MINUTES.toMillis(durationMinutes))
                showPauseDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsItem(icon = Icons.Default.Person, text = "Perfil") {
            navController.navigate("profile_screen")
        }
        if (userRole is UserRole.Supervised) {
            SettingsToggleItem(
                icon = Icons.Default.BluetoothSearching,
                text = if (isMonitoringPaused) "Monitoramento Pausado" else "Monitoramento do Anel",
                checked = isBleServiceRunning && !isMonitoringPaused,
                onCheckedChange = { isEnabled ->
                    viewModel.toggleBleService(context, isEnabled)
                },
                onPauseClick = { showPauseDialog = true },
                isServiceRunning = isBleServiceRunning,
                isPaused = isMonitoringPaused
            )
            SettingsItem(icon = Icons.Default.Sensors, text = "Configurar Sensores do Anel") {
                navController.navigate("ble_sensor_settings_screen")
            }
            SettingsItem(icon = Icons.Default.VolumeUp, text = "Monitoramento noturno") {
                navController.navigate("night_monitoring_screen")
            }
            SettingsItem(icon = Icons.Default.Call, text = "Contatos de emergência") {
                navController.navigate("emergency_contacts_screen")
            }
            SettingsItem(icon = Icons.Default.Sensors, text = "Metas e Alertas") {
                navController.navigate("sensors_settings_screen")
            }
        }
        SettingsItem(icon = Icons.Default.ExitToApp, text = "Sair") { /* Lógica de logout aqui */ }
        SettingsItem(icon = Icons.Default.DataObject, text = "Dados dos Sensores (Debug)") {
            navController.navigate("sensor_data_screen")
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TealColor,
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Ir para $text")
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onPauseClick: () -> Unit,
    isServiceRunning: Boolean,
    isPaused: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TealColor)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onPauseClick, enabled = isServiceRunning && !isPaused) {
                Icon(Icons.Default.Pause, contentDescription = "Pausar Monitoramento", tint = if (isServiceRunning && !isPaused) Color.White else Color.Gray)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = !isPaused,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )
        }
    }
}

@Composable
fun PauseDialog(
    onDismiss: () -> Unit,
    onConfirm: (durationMinutes: Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pausar Monitoramento") },
        text = {
            Column {
                Text("Escolha por quanto tempo deseja pausar o monitoramento do anel.")
            }
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                TextButton(onClick = { onConfirm(30L) }) { Text("30 minutos") }
                TextButton(onClick = { onConfirm(60L) }) { Text("1 hora") }
                TextButton(onClick = { onConfirm(120L) }) { Text("2 horas") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}