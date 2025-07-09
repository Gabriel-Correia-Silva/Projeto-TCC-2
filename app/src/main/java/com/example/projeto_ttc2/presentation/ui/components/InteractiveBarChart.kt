package com.example.projeto_ttc2.presentation.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class BarData(val value: Float, val label: String)

@Composable
fun InteractiveBarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { List(data.size) { Animatable(0f) } }

    LaunchedEffect(data) {
        animationProgress.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, delayMillis = index * 50)
                )
            }
        }
    }

    val maxValue = remember(data) { (data.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f) }

    val textPaint = remember(onSurfaceColor) {
        Paint().apply {
            color = onSurfaceColor.hashCode()
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
        }
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectHorizontalDragGestures(
                        onDragEnd = { selectedIndex = null },
                        onHorizontalDrag = { change, _ ->
                            val touchX = change.position.x
                            val barWidthWithSpacing = size.width / data.size
                            val index = (touchX / barWidthWithSpacing)
                                .toInt()
                                .coerceIn(0, data.size - 1)
                            selectedIndex = index
                        }
                    )
                }
        ) {
            val yAxisSpace = with(density) { 40.dp.toPx() }
            val xAxisSpace = with(density) { 30.dp.toPx() }
            val chartWidth = size.width - yAxisSpace
            val chartHeight = size.height - xAxisSpace
            val barWidthWithSpacing = chartWidth / data.size
            val barWidth = barWidthWithSpacing * 0.6f

            val numGridLines = 4
            (0..numGridLines).forEach { i ->
                val value = maxValue / numGridLines * i
                val y = chartHeight - (value / maxValue * chartHeight)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(yAxisSpace, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        value.toInt().toString(),
                        yAxisSpace / 2,
                        y + textPaint.textSize / 2,
                        textPaint
                    )
                }
            }

            data.forEachIndexed { index, barData ->
                val x = yAxisSpace + (barWidthWithSpacing * index) + (barWidthWithSpacing - barWidth) / 2
                val barHeight = (barData.value / maxValue * chartHeight) * animationProgress[index].value
                val barGradient = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.6f), primaryColor)
                )

                if (barHeight > 0) {
                    drawRoundRect(
                        brush = barGradient,
                        topLeft = Offset(x, chartHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }

                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        barData.label,
                        x + barWidth / 2,
                        size.height - (xAxisSpace / 4),
                        textPaint
                    )
                }
            }

            selectedIndex?.let { index ->
                val barData = data[index]
                val x = yAxisSpace + (barWidthWithSpacing * index) + barWidthWithSpacing / 2

                drawLine(
                    color = onSurfaceColor,
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 2f
                )

                val tooltipText = "${barData.value.toInt()} passos"
                val textWidth = textPaint.measureText(tooltipText)
                val tooltipPath = Path().apply {
                    val padding = 8.dp.toPx()
                    val rectHeight = textPaint.textSize + padding * 2
                    val rectWidth = textWidth + padding * 2
                    val rectX = (x - rectWidth / 2).coerceIn(0f, size.width - rectWidth)
                    val rectY = 0f

                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(rectX, rectY, rectX + rectWidth, rectY + rectHeight),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    )
                }

                drawPath(tooltipPath, color = onSurfaceColor)
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        tooltipText,
                        x.coerceIn(textWidth / 2 + 8.dp.toPx(), size.width - textWidth / 2 - 8.dp.toPx()),
                        textPaint.textSize + 8.dp.toPx(),
                        textPaint.apply { color = Color.White.hashCode() }
                    )
                }
            }
        }
    }
}