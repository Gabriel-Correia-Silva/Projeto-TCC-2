package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.projeto_ttc2.presentation.ui.navigation.Routes

@Composable
fun Header(navController: NavHostController, userName: String) {


    Row(
        modifier = Modifier
            .fillMaxWidth()             // ocupa toda a largura
            .padding(vertical = 8.dp),  // espaçamento vertical
        verticalAlignment = Alignment.CenterVertically,        // alinha verticalmente ao centro
        horizontalArrangement = Arrangement.SpaceBetween       // distribui espaço entre os elementos
    ) {
        // Texto de saudação: "Oi {userName}!"
        Text(
            text = "Oi $userName!",
            fontSize = 24.sp,
            color = Color(0xFF007C91)
        )

        // Container de ícones à direita
        Row {
            // Ícone de notificações
            IconButton(onClick = { /* TODO: ação de notificações */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificações",
                    tint = Color(0xFF007C91)
                )
            }

            // Ícone de configurações
            IconButton(onClick = {
                navController.navigate(Routes.SETTINGS)
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurações",
                    tint = Color(0xFF007C91)
                )
            }
        }
    }
}

