package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExerciseCard() {
    DashboardCard {
        Text("Exercícios", color = Color.White, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("01", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Exercícios na semana", color = Color.White, fontSize = 14.sp)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("01h", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Duração", color = Color.White, fontSize = 14.sp)
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.5f)
        )
        Text("Último exercício: Caminhada - 1h 10min - 298 kcal", color = Color.White, fontSize = 14.sp)
    }
}