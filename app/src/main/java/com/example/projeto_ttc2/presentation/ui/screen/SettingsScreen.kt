package com.example.projeto_ttc2.presentation.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.BluetoothSearching 
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext 
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projeto_ttc2.background.BleMonitoringService 
import com.example.projeto_ttc2.presentation.state.UserRole

val TealColor = Color(0xFF4DB6AC)

@Composable
fun SettingsScreen(
    navController: NavController,
    userRole: UserRole?
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsItem(icon = Icons.Default.Person, text = "Perfil") {
            navController.navigate("profile_screen")
        }
        if(userRole is UserRole.Supervised){
            SettingsItem(icon = Icons.Default.VolumeUp, text = "Monitoramento noturno") {
                navController.navigate("night_monitoring_screen")
            }
            SettingsItem(icon = Icons.Default.Call, text = "Contatos de emergência") {
                navController.navigate("emergency_contacts_screen")
            }
            SettingsItem(icon = Icons.Default.Sensors, text = "Metas e Alertas") {
                navController.navigate("sensors_settings_screen")
            }
          
            SettingsItem(icon = Icons.Default.BluetoothSearching, text = "Conectar Anel Colmi") {
              
                val intent = Intent(context, BleMonitoringService::class.java).apply {
                    action = BleMonitoringService.ACTION_START_BLE_MONITORING
                }
                context.startService(intent)
               
            }
          
            SettingsItem(icon = Icons.Default.BluetoothSearching, text = "Parar Monitoramento Anel Colmi") {
                val intent = Intent(context, BleMonitoringService::class.java).apply {
                    action = BleMonitoringService.ACTION_STOP_BLE_MONITORING
                }
                context.startService(intent)
            }
           
            SettingsItem(icon = Icons.Default.Sensors, text = "Configurar Sensores do Anel") {
                navController.navigate("ble_sensor_settings_screen")
            }
        }

        SettingsItem(icon = Icons.Default.ExitToApp, text = "Sair") {  }
        SettingsItem(icon = Icons.Default.Sensors, text = "Dados dos Sensores (App)") { 
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = rememberNavController(), userRole = UserRole.Supervised)
}