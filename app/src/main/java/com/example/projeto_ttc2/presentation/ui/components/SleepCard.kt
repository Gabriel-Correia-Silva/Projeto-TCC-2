package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.TimeUnit

@Composable
fun SleepCard(durationMinutes: Long) {
    val hours = TimeUnit.MINUTES.toHours(durationMinutes)
    val minutes = durationMinutes % 60
    val durationText = if (hours > 0 || minutes > 0) "${hours}h ${minutes}min" else "Nenhum dado"
    DashboardCard {
        Text("Sono", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Text("Gráfico de Sono Aqui", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Duração do sono: $durationText", color = Color.White, fontSize = 14.sp)
            Text("Pontuação: --", color = Color.White, fontSize = 14.sp)
        }
    }
}