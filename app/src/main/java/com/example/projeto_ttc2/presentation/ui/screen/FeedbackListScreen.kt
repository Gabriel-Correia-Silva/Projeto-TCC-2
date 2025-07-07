package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.database.entities.Feedback
import com.example.projeto_ttc2.presentation.viewmodel.FeedbackViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FeedbackListScreen(
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()

    // CORREÇÃO:
    // O LaunchedEffect agora "observa" a feedbackList.
    // Ele será executado sempre que a lista for atualizada a partir do Firestore.
    LaunchedEffect(feedbackList) {
        // Verificamos se há algum item não lido na lista que acabámos de receber.
        val hasUnreadItems = feedbackList.any { !it.read }
        if (hasUnreadItems) {
            // Se houver, chamamos a função para os marcar como lidos.
            viewModel.markAllAsRead()
        }
    }

    if (feedbackList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhum feedback recebido.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(feedbackList) { feedback ->
                FeedbackItem(
                    message = feedback.message,
                    timestamp = feedback.timestamp?.let {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                    } ?: "",
                    // O nome do campo foi corrigido aqui para corresponder ao modelo de dados.
                    isUnread = !feedback.read
                )
            }
        }
    }
}

@Composable
fun FeedbackItem(message: String, timestamp: String, isUnread: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Ícone de feedback",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (isUnread) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "Não lido",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp).padding(start = 8.dp)
                )
            }
        }
    }
}