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
    DashboardCard(
        cardColor = cardColor
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Bloodtype,
                contentDescription = "Ícone de Oxigenação",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Oxigenação", color = Color.White, fontWeight = FontWeight.Bold)
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
            }

            if (historicalSpo2.isNotEmpty()) {
                OxygenationBarChart(
                    data = historicalSpo2,
                    modifier = Modifier.height(60.dp).fillMaxWidth(0.9f)
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
    val barColor = Color.White

    val fixedBarHeight = with(density) { 30.dp.toPx() }
    val maxBarWidth = with(density) { 8.dp.toPx() }

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val dynamicChartWidth = (size.width / 7) * data.size

        val barWidthWithSpacing = dynamicChartWidth / data.size
        val barWidth = (barWidthWithSpacing * 0.5f).coerceAtMost(maxBarWidth)

        data.forEachIndexed { index, value ->
            val x = barWidthWithSpacing * index

            if (value > 0) {
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - fixedBarHeight),
                    size = Size(barWidth, fixedBarHeight)
                )
            }
        }
    }
}