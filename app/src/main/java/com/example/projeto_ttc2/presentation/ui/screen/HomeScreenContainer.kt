package com.example.projeto_ttc2.presentation.ui.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projeto_ttc2.presentation.ui.components.AlertItem
import com.example.projeto_ttc2.presentation.ui.components.AlertType
import com.example.projeto_ttc2.presentation.ui.components.AlertsCard
import com.example.projeto_ttc2.presentation.ui.components.CaloriesCard
import com.example.projeto_ttc2.presentation.ui.components.Header
import com.example.projeto_ttc2.presentation.ui.components.HeartRateCard
import com.example.projeto_ttc2.presentation.ui.components.SearchBar
import com.example.projeto_ttc2.presentation.ui.components.SleepCard
import com.example.projeto_ttc2.presentation.ui.components.StepsCard
import com.example.projeto_ttc2.presentation.ui.components.ExercisesCard
import com.example.projeto_ttc2.presentation.ui.components.FloatingCallButton
import com.example.projeto_ttc2.presentation.ui.components.Session
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.projeto_ttc2.presentation.ui.navigation.Routes

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreenContainer(navController: NavHostController) {
    var refreshing by remember { mutableStateOf(false) }
    var texto by remember { mutableStateOf("Sem dados ainda") }

    val refreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            texto = "Atualizado em ${System.currentTimeMillis()}"
            refreshing = false
        }
    )

    Box(
        modifier = Modifier.pullRefresh(refreshState)
            .fillMaxSize()
            .background(Color(0xFFE5F6F8)) // fundo geral
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Header(navController = navController,userName = "Maria")
            Spacer(modifier = Modifier.height(8.dp))
            SearchBar(placeholder = "Pesquisar")
            Spacer(modifier = Modifier.height(16.dp))
            StepsCard(
                currentSteps = 350,
                goalSteps = 8000,
                distanceKm = 0.8f,
                calories = 0.8f,
                onClick = { navController.navigate(Routes.STEPS) },
                NavController = navController
            )
            Spacer(modifier = Modifier.height(8.dp))
            CaloriesCard(
                consumed = 90,
                activityPercent = 12,
                onClick = { /* ação ao clicar */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            HeartRateCard(
                bpm = 79,
                status = "Relaxado",
                onClick = { /* ação ao clicar */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SleepCard(
                durationHours = 7,
                durationMinutes = 32,
                score = 89,
                onClick = { /* ação ao clicar */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExercisesCard(
                weeklyCount = 1,
                totalDurationHours = 1,
                sessions = listOf(Session("1h 10min", 298)),
                onClick = { /* ação ao clicar */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AlertsCard(
                alerts = listOf(
                    AlertItem(type = AlertType.QUEDA, text = "Alerta de queda"),
                    AlertItem(type = AlertType.CHAMADA, text = "Filha Rosa", date = "05/02")
                ),
                onClick = { /* ação ao clicar */ }
            )
            Spacer(modifier = Modifier.height(80.dp)) // espaço para o botão
        }

        PullRefreshIndicator(
            refreshing = refreshing,     // Liga o indicador ao estado de refresco
            state = refreshState,        // Usa o mesmo estado para animações corretas
            modifier = Modifier
                .align(Alignment.TopCenter) // Posiciona o indicador no topo-centro do Box
        )



    FloatingCallButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            onClick = { /* lógica de chamada */ }
        )
    }
}