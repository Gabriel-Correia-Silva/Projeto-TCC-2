package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SleepCard: exibe sono e pontuação.
 *
 * @param durationHours horas de sono.
 * @param durationMinutes minutos de sono.
 * @param score pontuação de qualidade do sono.
 */
@Composable
fun SleepCard(
    durationHours: Int,
    durationMinutes: Int,
    score: Int,
    onClick: () -> Unit
) {
    // Calcula total de horas em decimal (horas + fração de minutos)
    val total = durationHours + durationMinutes / 60f
    // Progresso relativo à meta de 8h (valor de 0f a 1f)
    val progressFraction = total / 8f

    Column(
        modifier = Modifier
            .fillMaxWidth()                                   // ocupa toda a largura disponível
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp)) // fundo azul e cantos arredondados
            .padding(16.dp)                                   // padding interno
    ) {
        // Título do card
        Text(
            text = "Sono",
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progresso linear (uso da API recomendada: progress como lambda)
        LinearProgressIndicator(
            progress = { progressFraction },                  // passa o valor via lambda
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color.White,                              // cor do indicador preenchido
            trackColor = Color.White.copy(alpha = 0.3f)       // cor do trilho (fundo da barra)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Texto com duração formatada
        Text(
            text = "Duração do sono: ${durationHours}h ${durationMinutes}min ▲",
            fontSize = 14.sp,
            color = Color.White
        )
        // Texto com pontuação
        Text(
            text = "Pontuação: $score",
            fontSize = 14.sp,
            color = Color.White
        )
    }
}
