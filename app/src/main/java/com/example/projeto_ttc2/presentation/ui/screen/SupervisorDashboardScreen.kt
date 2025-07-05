// Altere o arquivo: app/src/main/java/com/example/projeto_ttc2/presentation/ui/screen/SupervisorDashboardScreen.kt
package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.presentation.ui.components.UserCard
import com.example.projeto_ttc2.presentation.viewmodel.HealthSummary
import com.example.projeto_ttc2.presentation.viewmodel.SupervisorViewModel

@Composable
fun SupervisorDashboardScreen(
    viewModel: SupervisorViewModel = hiltViewModel() // Injeta o ViewModel
) {
    // Coleta os dados do ViewModel como um estado
    val supervisedUsers by viewModel.supervisedUsers.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val healthData by viewModel.healthData.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pesquisar Supervisionado") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = { /* Ação do botão */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pacientes")
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(supervisedUsers.filter { it.name?.contains(searchQuery, ignoreCase = true) == true }) { user ->
                val summary = healthData[user.id] ?: HealthSummary()
                UserCard(
                    user = user,
                    steps = summary.steps,
                    heartRate = summary.heartRate,
                    sleep = "${summary.sleep / 60}h ${summary.sleep % 60}m",
                    calories = summary.calories,
                    activity = 0, // Defina a lógica de atividade se desejar
                    onChatClick = { /* ação do chat */ }
                )
            }
        }
    }
    }
