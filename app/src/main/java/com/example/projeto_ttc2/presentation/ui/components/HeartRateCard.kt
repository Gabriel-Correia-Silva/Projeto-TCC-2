package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
            .fillMaxWidth()
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {

        Text(
            text = "Frequência",
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))


        Text(
            text = "$bpm bpm",
            fontSize = 32.sp,
            color = Color.White
        )


        Text(
            text = "Tipo da frequência: $status",
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))


        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            if (sampleData.size < 2) return@Canvas


            val stepX = size.width / (sampleData.size - 1)

            val minY = sampleData.minOrNull() ?: 0f
            val maxY = sampleData.maxOrNull() ?: 0f
            val rangeY = (maxY - minY).takeIf { it > 0f } ?: 1f


            val path = Path().apply {

                moveTo(0f,
                    size.height - ((sampleData[0] - minY) / rangeY) * size.height
                )

                sampleData.drop(1).forEachIndexed { index, value ->
                    val x = (index + 1) * stepX
                    val y = size.height - ((value - minY) / rangeY) * size.height
                    lineTo(x, y)
                }
            }


            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
