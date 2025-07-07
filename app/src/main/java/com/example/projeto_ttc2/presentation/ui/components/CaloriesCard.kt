package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.presentation.ui.theme.TealGreen
import kotlin.math.roundToInt

@Composable
fun CaloriesCard(
    activeKcal: Double,
    totalKcal: Double,
    cardColor: Color = TealGreen
) {
    val progress = if (totalKcal > 0) (activeKcal.toFloat() / totalKcal.toFloat()) else 0f
    DashboardCard(
        cardColor = cardColor
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Ícone de Calorias",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calorias", color = Color.White, fontWeight = FontWeight.Bold)
        }
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