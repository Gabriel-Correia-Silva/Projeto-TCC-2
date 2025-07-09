package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.presentation.ui.theme.TealGreen

@Composable
fun OxygenationCard(
    spo2: Double,
    historicalSpo2: List<Double> = emptyList(),
    cardColor: Color = TealGreen
) {
    val statusText: String
    val statusColor: Color

    when {
        spo2 >= 95 -> {
            statusText = "Normal"
        }
        spo2 >= 90 -> {
            statusText = "Atenção"
        }
        spo2 > 0 -> {
            statusText = "Baixo"
        }
        else -> {
            statusText = "Sem dados"
        }
    }

    DashboardCard(
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Bloodtype,
                contentDescription = "Ícone de Oxigenação",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("SpO2", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Última leitura",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (spo2 > 0) "${spo2.toInt()}" else "--",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                    )
                }
                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (historicalSpo2.isNotEmpty()) {
                OxygenationBarChart(
                    data = historicalSpo2,
                    modifier = Modifier.height(60.dp).fillMaxWidth(0.6f)
                )
            }
        }
    }
}

@Composable
fun OxygenationBarChart(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val barColor = Color.White.copy(alpha = 0.8f)

    val maxSpo2 = 100.0
    val minSpo2 = (data.minOrNull() ?: 85.0).coerceAtMost(85.0)
    val range = (maxSpo2 - minSpo2).coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val barWidthWithSpacing = size.width / data.size
        val barWidth = barWidthWithSpacing * 0.5f

        data.forEachIndexed { index, value ->
            val normalizedValue = ((value - minSpo2) / range).coerceIn(0.0, 1.0)
            val barHeight = (size.height * normalizedValue).toFloat()
            val x = barWidthWithSpacing * index + (barWidthWithSpacing - barWidth) / 2

            if (value > 0) {
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
    }
}