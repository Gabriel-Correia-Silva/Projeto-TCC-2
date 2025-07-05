package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import com.example.projeto_ttc2.presentation.viewmodel.Period
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

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

    // Carga inicial dos dados
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
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { dashboardViewModel.setPeriod(it) }
            )

            // Lógica para exibir o gráfico correto
            if (selectedPeriod == Period.DIA) {
                if (hourlySteps.isNotEmpty()) {
                    // Função que estava faltando, agora adicionada abaixo
                    StepsBarChart(dataByHour = hourlySteps, modifier = Modifier.fillMaxWidth().height(250.dp))
                } else {
                    EmptyState()
                }
            } else {
                if (periodStepsData.isNotEmpty()) {
                    PeriodStepsBarChart(data = periodStepsData, period = selectedPeriod, modifier = Modifier.fillMaxWidth().height(250.dp))
                } else {
                    EmptyState()
                }
            }
        }
    }
}

// **A FUNÇÃO QUE ESTAVA FALTANDO FOI ADICIONADA AQUI**
@Composable
fun StepsBarChart(
    dataByHour: Map<Int, Long>,
    modifier: Modifier = Modifier
) {
    if (dataByHour.isEmpty()) return

    val maxSteps = (dataByHour.values.maxOrNull() ?: 1000L).toFloat()
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val textPaint = android.graphics.Paint().apply {
        color = onSurfaceColor.hashCode()
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = with(density) { 12.sp.toPx() }
    }

    Canvas(modifier = modifier) {
        val yAxisSpace = 40.dp.toPx()
        val xAxisSpace = 30.dp.toPx()
        val chartWidth = size.width - yAxisSpace
        val chartHeight = size.height - xAxisSpace

        // Eixo Y
        val numGridLines = 4
        for (i in 0..numGridLines) {
            val value = (maxSteps / numGridLines) * i
            val y = chartHeight - (value / maxSteps * chartHeight)
            drawLine(
                color = Color.LightGray,
                start = Offset(yAxisSpace, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${value.toInt()}",
                yAxisSpace / 2,
                y + textPaint.textSize / 2,
                textPaint
            )
        }

        // Eixo X e Barras
        val barWidthWithSpacing = chartWidth / 24
        val barWidth = barWidthWithSpacing * 0.7f

        for (hour in 0..23) {
            val steps = dataByHour[hour]?.toFloat() ?: 0f
            val x = yAxisSpace + (barWidthWithSpacing * hour)

            if (steps > 0) {
                val barHeight = (steps / maxSteps * chartHeight).coerceAtLeast(0f)
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }

            if (hour % 3 == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    hour.toString().padStart(2, '0'),
                    x + barWidth / 2,
                    size.height - (xAxisSpace / 4),
                    textPaint
                )
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
        Text("Sem dados de passos para este período.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TotalStepsCard(
    steps: Long,
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
                        // Correção: Usar .entries em vez de .values()
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
                    Text("Distância: -- km", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                    Text("Tempo total: --", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { /* TODO: Share action */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun PeriodStepsBarChart(data: List<Passos>, period: Period, modifier: Modifier = Modifier) {
    val maxSteps = (data.maxOfOrNull { it.contagem } ?: 1000L).toFloat()
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val textPaint = android.graphics.Paint().apply {
        color = onSurfaceColor.hashCode()
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = with(density) { 12.sp.toPx() }
    }

    val labels = when (period) {
        Period.SEMANA -> DayOfWeek.values().map { it.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).take(3) }
        Period.MES -> (1..data.size).map { it.toString() }
        else -> emptyList()
    }

    val dataMap = data.associateBy { it.data.dayOfWeek }

    Canvas(modifier = modifier) {
        val yAxisSpace = 40.dp.toPx()
        val xAxisSpace = 30.dp.toPx()
        val chartWidth = size.width - yAxisSpace
        val chartHeight = size.height - xAxisSpace

        // Eixo Y
        (0..4).forEach { i ->
            val value = maxSteps / 4 * i
            val y = chartHeight - (value / maxSteps * chartHeight)
            drawContext.canvas.nativeCanvas.drawText("${value.toInt()}", yAxisSpace / 2, y + textPaint.textSize / 2, textPaint)
        }

        // Eixo X e Barras
        val barCount = if (period == Period.SEMANA) 7 else data.size
        val barWidthWithSpacing = chartWidth / barCount
        val barWidth = barWidthWithSpacing * 0.6f

        (0 until barCount).forEach { index ->
            val day = DayOfWeek.values()[index]
            val steps = (if (period == Period.SEMANA) dataMap[day]?.contagem else data.getOrNull(index)?.contagem) ?: 0L
            val x = yAxisSpace + (barWidthWithSpacing * index) + (barWidthWithSpacing - barWidth) / 2

            if (steps > 0) {
                val barHeight = (steps / maxSteps * chartHeight).coerceAtLeast(0f)
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }

            drawContext.canvas.nativeCanvas.drawText(labels[index], x + barWidth / 2, size.height - (xAxisSpace / 4), textPaint)
        }
    }
}

@Composable
private fun EmergencyCallFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        modifier = Modifier.size(72.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Ligação de emergência",
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun ActivitiesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Atividades",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            ActivityItem(name = "Atividade leve", duration = "0h 15min", date = "05/02")
            ActivityItem(name = "Atividade leve", duration = "0h 35min", date = "05/02")
        }
    }
}

@Composable
private fun ActivityItem(name: String, duration: String, date: String) {
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsWalk,
            contentDescription = null,
            tint = onPrimaryColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = onPrimaryColor, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = onPrimaryColor.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(duration, color = onPrimaryColor.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
        Text(date, color = onPrimaryColor.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}