package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.projeto_ttc2.presentation.ui.components.UserCard
import com.example.projeto_ttc2.presentation.viewmodel.HealthSummary
import com.example.projeto_ttc2.presentation.viewmodel.SupervisorViewModel

@Composable
fun SupervisorDashboardScreen(
    navController: NavController,
    viewModel: SupervisorViewModel = hiltViewModel()
) {
    val supervisedUsers by viewModel.supervisedUsers.collectAsStateWithLifecycle()
    val healthData by viewModel.healthData.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val isLoading = supervisedUsers.isEmpty() && healthData.isEmpty()

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
                .padding(vertical = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (supervisedUsers.isEmpty()) {
            EmptySupervisedList()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredUsers = supervisedUsers.filter {
                    it.name?.contains(searchQuery, ignoreCase = true) == true
                }
                items(filteredUsers) { user ->
                    val summary = healthData[user.id] ?: HealthSummary()
                    UserCard(
                        user = user,
                        steps = summary.steps,
                        heartRate = summary.heartRate,
                        sleep = "${summary.sleep / 60}h ${summary.sleep % 60}m",
                        calories = summary.calories,
                        onDetailsClick = {
                            navController.navigate("patient_detail/${user.id}")
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun EmptySupervisedList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PeopleOutline,
            contentDescription = "Nenhum supervisionado",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum supervisionado encontrado",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "Peça para que eles adicionem seu ID de supervisor no momento do cadastro.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}