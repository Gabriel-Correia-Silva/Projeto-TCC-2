package com.example.projeto_ttc2.presentation.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CaloriesCard: mostra calorias consumidas.
 * @param consumed kcal consumidas.
 * @param activityPercent % de atividade.
 */
@Composable
fun CaloriesCard(
    consumed: Int,
    activityPercent: Int,
    onClick: () -> Unit
) {
    val progress = consumed / 2000f // meta assumida
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Text("Calorias", fontSize = 18.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            backgroundColor = Color.White.copy(alpha = 0.3f),
            color = Color.White
        )
        Spacer(Modifier.height(12.dp))
        Text("$consumed kcal", fontSize = 14.sp, color = Color.White)
        Text("Consumo de atividade: $activityPercent% ▼", fontSize = 14.sp, color = Color.White)
    }
}