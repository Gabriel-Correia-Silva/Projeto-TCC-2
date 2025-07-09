package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.database.entities.FallEvent
import com.example.projeto_ttc2.presentation.viewmodel.FallHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FallHistoryScreen(
    viewModel: FallHistoryViewModel = hiltViewModel()
) {
    val fallEvents by viewModel.fallEvents.collectAsStateWithLifecycle()

    if (fallEvents.isEmpty()) {
        EmptyContentState(message = "Nenhum alerta de queda registrado.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(fallEvents) { event ->
                FallEventCard(event = event)
            }
        }
    }
}

@Composable
fun FallEventCard(event: FallEvent) {
    val formatter = remember { SimpleDateFormat("dd 'de' MMMM, yyyy 'às' HH:mm", Locale("pt", "BR")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Alerta de Queda",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Possível Queda Detectada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatter.format(event.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.details,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}