package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun HeartRateCard(
    bpm: Long,
    heartRateData: List<Long> = emptyList(),
    onClick: () -> Unit
) {
    DashboardCard(
        onClick = onClick // Use o onClick do DashboardCard ao invés do Modifier.clickable
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Frequência",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (bpm > 0) "$bpm" else "--",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "bpm",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }

            HeartRateChart(
                data = heartRateData,
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Ocupa 60% da largura disponível
                    .height(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            if (bpm > 0) "Última leitura" else "Nenhum dado",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun HeartRateChart(
    data: List<Long>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) {
            drawEmptyChart()
            return@Canvas
        }

        if (data.size == 1) {
            drawSinglePointChart(data[0])
            return@Canvas
        }

        drawHeartRateChart(data)
    }
}

private fun DrawScope.drawEmptyChart() {
    // Linha de base sutil
    drawLine(
        color = Color.White.copy(alpha = 0.2f),
        start = Offset(0f, size.height * 0.7f),
        end = Offset(size.width, size.height * 0.7f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
    )

    // Texto indicativo
    drawContext.canvas.nativeCanvas.apply {
        drawText(
            "Sem dados",
            size.width / 2f,
            size.height / 2f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                alpha = 100
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 12.dp.toPx()
            }
        )
    }
}

private fun DrawScope.drawSinglePointChart(bpm: Long) {
    val centerY = size.height * 0.5f
    val centerX = size.width * 0.5f

    // Ponto único
    drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = Offset(centerX, centerY)
    )

    // Linha de base
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(0f, centerY),
        end = Offset(size.width, centerY),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawHeartRateChart(data: List<Long>) {
    val maxValue = data.maxOrNull()?.toFloat() ?: 100f
    val minValue = data.minOrNull()?.toFloat() ?: 60f
    val range = (maxValue - minValue).takeIf { it > 0 } ?: 40f

    // Adiciona padding vertical para melhor visualização
    val paddingTop = size.height * 0.1f
    val paddingBottom = size.height * 0.1f
    val chartHeight = size.height - paddingTop - paddingBottom

    val stepX = size.width / (data.size - 1).coerceAtLeast(1)

    // Pontos do gráfico
    val points = data.mapIndexed { index, bpm ->
        val x = index * stepX
        val normalizedY = (bpm - minValue) / range
        val y = size.height - paddingBottom - (normalizedY * chartHeight)
        Offset(x, y)
    }

    // Desenha área preenchida (gradiente)
    val path = Path().apply {
        moveTo(points.first().x, size.height)
        points.forEach { point -> lineTo(point.x, point.y) }
        lineTo(points.last().x, size.height)
        close()
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.1f),
            Color.Transparent
        ),
        startY = 0f,
        endY = size.height
    )

    drawPath(path = path, brush = gradient)

    // Desenha linha principal suavizada
    val smoothPath = createSmoothPath(points)
    drawPath(
        path = smoothPath,
        color = Color.White,
        style = Stroke(
            width = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Desenha pontos de dados
    points.forEachIndexed { index, point ->
        val isFirstOrLast = index == 0 || index == points.lastIndex
        val radius = if (isFirstOrLast) 3.dp.toPx() else 2.dp.toPx()
        val alpha = if (isFirstOrLast) 1f else 0.8f

        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = point
        )
    }

    // Linhas de grade sutis
    drawGridLines(minValue, maxValue, paddingTop, paddingBottom, chartHeight)
}

private fun createSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points[0].x, points[0].y)

    for (i in 1 until points.size) {
        val currentPoint = points[i]
        val previousPoint = points[i - 1]

        // Controle de suavização simples
        val controlPointX = previousPoint.x + (currentPoint.x - previousPoint.x) * 0.5f

        path.quadraticTo(
            controlPointX, previousPoint.y,
            currentPoint.x, currentPoint.y
        )
    }

    return path
}

private fun DrawScope.drawGridLines(
    minValue: Float,
    maxValue: Float,
    paddingTop: Float,
    paddingBottom: Float,
    chartHeight: Float
) {
    val gridLines = 3
    val range = maxValue - minValue

    repeat(gridLines) { i ->
        val value = minValue + (range * (i + 1) / (gridLines + 1))
        val normalizedY = (value - minValue) / range
        val y = size.height - paddingBottom - (normalizedY * chartHeight)

        drawLine(
            color = Color.White.copy(alpha = 0.15f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 0.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))
        )
    }
}