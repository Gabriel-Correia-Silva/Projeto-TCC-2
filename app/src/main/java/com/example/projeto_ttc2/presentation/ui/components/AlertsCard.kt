package com.example.projeto_ttc2.presentation.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment

enum class AlertType { QUEDA, CHAMADA }
data class AlertItem(val type: AlertType, val text: String, val date: String? = null)

/**
 * AlertsCard: exibe alertas recebidos.
 * @param alerts lista de AlertItem.
 */
@Composable
fun AlertsCard(alerts: List<AlertItem>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF007C91), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text("Alertas", fontSize = 18.sp, color = Color.White)
        alerts.forEach { a ->
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = if (a.type == AlertType.QUEDA) Icons.Default.Warning else Icons.Default.Phone
                Icon(icon, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(a.text, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f))
                if (a.date != null) {
                    Text(a.date, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}