package com.example.projeto_ttc2.presentation.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

data class Session(val duration: String, val calories: Int)

/**
 * ExercisesCard: lista de exercícios da semana.
 * @param weeklyCount quantidade de sessões.
 * @param totalDurationHours duração total em horas.
 * @param sessions lista de sessões.
 */
@Composable
fun ExercisesCard(
    weeklyCount: Int,
    totalDurationHours: Int,
    sessions: List<Session>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text("Exercícios", fontSize = 18.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = weeklyCount.toString().padStart(2, '0') + " Exercícios na semana",
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = "${totalDurationHours}h Duração dos exérc.",
                fontSize = 14.sp,
                color = Color.White
            )
        }
        sessions.forEach { s ->
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.duration, fontSize = 14.sp, color = Color.White)
                Text("${s.calories} kcal", fontSize = 14.sp, color = Color.White)
            }
        }
    }
}