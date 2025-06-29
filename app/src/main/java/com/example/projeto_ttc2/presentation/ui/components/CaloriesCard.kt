package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CaloriesCard(activeKcal: Double, totalKcal: Double) {
    val progress = if (totalKcal > 0) (activeKcal.toFloat() / totalKcal.toFloat()) else 0f
    DashboardCard {
        Text("Calorias", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        Text("${activeKcal.roundToInt()} kcal (ativos)", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Consumo total: ${totalKcal.roundToInt()} kcal", color = Color.White, fontSize = 14.sp)
    }
}