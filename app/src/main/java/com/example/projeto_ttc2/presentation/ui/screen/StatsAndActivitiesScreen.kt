package com.example.projeto_ttc2.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projeto_ttc2.presentation.ui.components.FloatingCallButton

@Composable
fun StatsAndActivitiesScreen(
    navController: NavHostController,
    totalSteps: Int,
    distanceKm: Float,
    totalTime: String,
    onShareClick: () -> Unit,
    activities: List<ActivityItem>,
    onPeriodChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top icons (exemplo estático)
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { /* segurança */ }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Status")
                }
                IconButton(onClick = { /* configurações */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurações")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card Total de passos
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0097A7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Total de passos",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        // Dropdown de período
                        var expanded by remember { mutableStateOf(false) }
                        val periods = listOf("Dia", "Semana", "Mês")
                        var selected by remember { mutableStateOf(periods[1]) }
                        Box {
                            Text(
                                text = selected,
                                color = Color.White,
                                modifier = Modifier
                                    .clickable { expanded = true }
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                periods.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p) },
                                        onClick = {
                                            selected = p
                                            expanded = false
                                            onPeriodChange(p)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = totalSteps.toString(),
                        fontSize = 36.sp,
                        color = Color.White
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Distância total: ${"%.1f".format(distanceKm)} km",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Tempo total: $totalTime",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card Atividades
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0097A7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Atividades",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))

                    activities.forEach { activity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = activity.label,
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = activity.label,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = activity.duration,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = activity.date,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        FloatingCallButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            onClick = { /* lógica de chamada */ }
        )
    }
}

data class ActivityItem(
    val label: String,
    val duration: String,
    val date: String
)