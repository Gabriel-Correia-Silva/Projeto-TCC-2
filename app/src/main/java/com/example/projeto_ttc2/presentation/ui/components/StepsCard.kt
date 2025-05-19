package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text
import androidx.navigation.NavHostController

/**
 * StepsCard: exibe passos e progresso.
 * @param currentSteps passos atuais.
 * @param goalSteps meta de passos.
 * @param distanceKm km percorridos.
 * @param calories calorias gastas.
 */
@Composable
fun StepsCard(
    NavController: NavHostController,
    currentSteps: Int,
    goalSteps: Int,
    distanceKm: Float,
    calories: Float,
    onClick: () -> Unit
) {
    val progress = currentSteps / goalSteps.toFloat()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp))
            .clickable { onClick() } // Adicione esta linha
            .padding(16.dp)
    ) {
        Text("Passos", fontSize = 18.sp, color = Color.White)
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
        Text("$currentSteps / $goalSteps passos", fontSize = 14.sp, color = Color.White)
        Text("Distância total: $distanceKm km", fontSize = 14.sp, color = Color.White)
        Text("Calorias: $calories kcal", fontSize = 14.sp, color = Color.White)
    }
}
