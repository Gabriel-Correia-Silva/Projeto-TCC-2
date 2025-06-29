package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardHeader(userName: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Oi, $userName!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row {
            IconButton(onClick = { /* TODO: Navegar para tela de segurança */ }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Status de Segurança",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { /* TODO: Navegar para tela de notificações */ }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificações",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Sair",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}