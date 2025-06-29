package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.R

@Composable
fun HeartRateCard(bpm: Long) {
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Frequência", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(if (bpm > 0) "$bpm" else "--", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("bpm", fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
            }
            Image(painter = painterResource(id = R.drawable.icons8_google_logo), contentDescription = "Gráfico de Frequência")
        }
        Text(if (bpm > 0) "Última leitura" else "Nenhum dado", color = Color.White, fontSize = 14.sp)
    }
}