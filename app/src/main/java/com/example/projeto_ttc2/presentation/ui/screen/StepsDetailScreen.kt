package com.example.projeto_ttc2.presentation.ui.screen

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import com.example.projeto_ttc2.presentation.viewmodel.Period
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

data class BarData(val value: Float, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsDetailScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel
) {
    val selectedPeriod by dashboardViewModel.selectedPeriod.collectAsStateWithLifecycle()
    val totalStepsForPeriod by dashboardViewModel.totalStepsForPeriod.collectAsStateWithLifecycle()
    val hourlySteps by dashboardViewModel.hourlyStepsForDate.collectAsStateWithLifecycle()
    val periodStepsData by dashboardViewModel.stepsForPeriod.collectAsStateWithLifecycle()
    val distanceKm by dashboardViewModel.todayDistanceKm.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        dashboardViewModel.setPeriod(Period.SEMANA)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TotalStepsCard(
                steps = totalStepsForPeriod,
                distanceKm = distanceKm,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { dashboardViewModel.setPeriod(it) }
            )

            val chartData = remember(selectedPeriod, hourlySteps, periodStepsData) {
                when (selectedPeriod) {
                    Period.DIA -> (0..23).map { hour ->
                        BarData(
                            value = hourlySteps[hour]?.toFloat() ?: 0f,
                            label = hour.toString().padStart(2, '0')
                        )
                    }
                    Period.SEMANA -> {
                        val dataMap = periodStepsData.associateBy { LocalDate.parse(it.data).dayOfWeek }
                        DayOfWeek.values().map { day ->
                            BarData(
                                value = dataMap[day]?.contagem?.toFloat() ?: 0f,
                                label = day.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).take(3)
                            )
                        }
                    }
                    Period.MES -> periodStepsData.map {
                        BarData(
                            value = it.contagem.toFloat(),
                            label = LocalDate.parse(it.data).dayOfMonth.toString()
                        )
                    }
                }
            }

            if (chartData.any { it.value > 0 }) {
                InteractiveBarChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } else {
                EmptyState()
            }
        }
    }
}

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
                            val index = (touchX / barWidthWithSpacing).toInt().coerceIn(0, data.size - 1)
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

            // Desenha o eixo Y e as linhas de grade
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

            // Desenha as barras e os rótulos do eixo X
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

            // Desenha a linha de destaque e o tooltip
            selectedIndex?.let { index ->
                val barData = data[index]
                val x = yAxisSpace + (barWidthWithSpacing * index) + barWidthWithSpacing / 2

                // Linha de destaque
                drawLine(
                    color = onSurfaceColor,
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 2f
                )

                // Tooltip
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

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Sem dados de passos para este período.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TotalStepsCard(
    steps: Long,
    distanceKm: Double,
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit
) {
    var periodExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total de passos",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = !periodExpanded }
                ) {
                    Row(
                        modifier = Modifier.menuAnchor(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedPeriod) {
                                Period.DIA -> "Dia"
                                Period.SEMANA -> "Semana"
                                Period.MES -> "Mês"
                            },
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar período", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        Period.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onPeriodSelected(period)
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = steps.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Distância: ${"%.2f".format(distanceKm)} km",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    // O tempo total pode ser calculado com base nos dados de atividade, se disponíveis
                    Text(
                        "Tempo total: --",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}