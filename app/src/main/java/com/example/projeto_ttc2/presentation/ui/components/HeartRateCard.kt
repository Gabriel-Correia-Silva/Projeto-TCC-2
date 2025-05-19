package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HeartRateCard: exibe batimentos cardíacos e um gráfico simples.
 *
 * @param bpm valor atual de batimentos por minuto.
 * @param status descrição do estado (por exemplo, "Relaxado").
 */
@Composable
fun HeartRateCard(
    bpm: Int,
    status: String,
    onClick: () -> Unit
) {
    // Dados de exemplo para o gráfico (varia ± alguns bpm)
    val sampleData = listOf(
        (bpm - 5).toFloat(),
        bpm.toFloat(),
        (bpm + 3).toFloat(),
        bpm.toFloat(),
        (bpm - 2).toFloat()
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()                                 // ocupa toda a largura do pai
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp)) // fundo azul e bordas arredondadas
            .padding(16.dp)                                  // espaçamento interno
    ) {
        // Título
        Text(
            text = "Frequência",
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Valor de BPM
        Text(
            text = "$bpm bpm",
            fontSize = 32.sp,
            color = Color.White
        )

        // Descrição do status
        Text(
            text = "Tipo da frequência: $status",
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Gráfico de linha simples usando Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            if (sampleData.size < 2) return@Canvas

            // Calcula espaçamento horizontal entre pontos
            val stepX = size.width / (sampleData.size - 1)
            // Normaliza valores para ajustar ao canvas verticalmente
            val minY = sampleData.minOrNull() ?: 0f
            val maxY = sampleData.maxOrNull() ?: 0f
            val rangeY = (maxY - minY).takeIf { it > 0f } ?: 1f

            // Monta o path do gráfico
            val path = Path().apply {
                // move para o primeiro ponto
                moveTo(0f,
                    size.height - ((sampleData[0] - minY) / rangeY) * size.height
                )
                // desenha linha para cada ponto seguinte
                sampleData.drop(1).forEachIndexed { index, value ->
                    val x = (index + 1) * stepX
                    val y = size.height - ((value - minY) / rangeY) * size.height
                    lineTo(x, y)
                }
            }

            // Desenha o path com stroke branco
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
