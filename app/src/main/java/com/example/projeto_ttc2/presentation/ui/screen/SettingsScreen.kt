package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

val TealColor = Color(0xFF4DB6AC)

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsItem(icon = Icons.Default.Person, text = "Perfil") {
            navController.navigate("profile_screen")
        }
        SettingsItem(icon = Icons.Default.VolumeUp, text = "Monitoramento noturno") { /* TODO */ }
        SettingsItem(icon = Icons.Default.Call, text = "Contatos de emergência") {
            navController.navigate("emergency_contacts_screen")
        }
        SettingsItem(icon = Icons.Default.Notifications, text = "Notificações") { /* TODO */ }
        SettingsItem(icon = Icons.Default.Sensors, text = "Sensores") { /* TODO */ }
        SettingsItem(icon = Icons.Default.Palette, text = "Tema") { /* TODO */ }
        SettingsItem(icon = Icons.Default.ExitToApp, text = "Sair") { /* TODO */ }
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
    SettingsScreen(navController = rememberNavController())
}