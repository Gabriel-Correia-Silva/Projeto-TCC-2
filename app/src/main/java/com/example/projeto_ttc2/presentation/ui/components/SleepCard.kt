package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SleepCard(
    durationHours: Int,
    durationMinutes: Int,
    score: Int,
    onClick: () -> Unit
) {

    val total = durationHours + durationMinutes / 60f

    val progressFraction = total / 8f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        // Título do card
        Text(
            text = "Sono",
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))


        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(12.dp))


        Text(
            text = "Duração do sono: ${durationHours}h ${durationMinutes}min ▲",
            fontSize = 14.sp,
            color = Color.White
        )

        Text(
            text = "Pontuação: $score",
            fontSize = 14.sp,
            color = Color.White
        )
    }
}
