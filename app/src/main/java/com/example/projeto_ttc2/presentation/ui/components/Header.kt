package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projeto_ttc2.presentation.ui.navigation.Routes

@Composable
fun Header(navController: NavHostController, userName: String) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Texto de saudação: "Oi {userName}!"
        Text(
            text = "Oi $userName!",
            fontSize = 24.sp,
            color = Color(0xFF007C91)
        )


        Row {

            IconButton(onClick = { /* TODO: ação de notificações */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificações",
                    tint = Color(0xFF007C91)
                )
            }


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

