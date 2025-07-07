package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.presentation.ui.theme.TealGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepCard(
    sleepSession: Sono?,
    onClick: () -> Unit,
    cardColor: Color = TealGreen
) {
    fun formatDuration(minutes: Long?): String {
        if (minutes == null || minutes <= 0) return "--"
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours}h ${mins}min"
    }

    val totalDurationText = formatDuration(sleepSession?.durationMinutes)

    DashboardCard(
        onClick = onClick,
        cardColor = cardColor
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = "Ícone de Sono",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sono", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Duração Total", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(totalDurationText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        SleepPhaseRow("Acordado", formatDuration(sleepSession?.awakeDurationMinutes))
        SleepPhaseRow("Sono REM", formatDuration(sleepSession?.remSleepDurationMinutes))
        SleepPhaseRow("Sono Leve", formatDuration(sleepSession?.lightSleepDurationMinutes))
        SleepPhaseRow("Sono Profundo", formatDuration(sleepSession?.deepSleepDurationMinutes))
    }
}

@Composable
private fun SleepPhaseRow(phase: String, duration: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(phase, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Text(duration, color = Color.White, fontSize = 14.sp)
    }
}