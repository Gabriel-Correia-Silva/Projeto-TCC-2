// Define o pacote onde este arquivo de tela está localizado no projeto.
package com.example.projeto_ttc2.presentation.ui.screen

// Importa os componentes e funcionalidades necessárias do Jetpack Compose.
import androidx.compose.foundation.layout.Arrangement // Para organizar os itens com espaçamento.
import androidx.compose.foundation.layout.Box // Um container que permite sobrepor componentes.
import androidx.compose.foundation.layout.PaddingValues // Para definir preenchimento (espaçamento interno).
import androidx.compose.foundation.layout.fillMaxSize // Para fazer um componente preencher todo o espaço disponível.
import androidx.compose.foundation.layout.padding // Para aplicar preenchimento a um componente.
import androidx.compose.foundation.lazy.LazyColumn // Para criar uma lista rolável de itens de forma eficiente.
import androidx.compose.material3.ExperimentalMaterial3Api // Anotação para usar APIs do Material 3 que ainda são experimentais.
import androidx.compose.material3.Scaffold // Estrutura de layout básica do Material Design (com suporte para barras, etc.).
import androidx.compose.material3.pulltorefresh.PullToRefreshBox // Componente que lida com a funcionalidade de "puxar para atualizar".
import androidx.compose.runtime.Composable // Anotação que marca uma função como um componente de UI do Compose.
import androidx.compose.ui.Modifier // Objeto para adicionar propriedades (como tamanho, cor, etc.) a um componente.
import androidx.compose.ui.unit.dp // Unidade de medida para especificar tamanhos e espaçamentos.
import androidx.navigation.NavController // Objeto para controlar a navegação entre telas.
import com.example.projeto_ttc2.database.local.DashboardData // Classe de dados que contém as informações do dashboard.
import com.example.projeto_ttc2.presentation.ui.components.CaloriesCard // Componente de UI para o card de calorias.
import com.example.projeto_ttc2.presentation.ui.components.DashboardHeader // Componente de UI para o cabeçalho do dashboard.
import com.example.projeto_ttc2.presentation.ui.components.HeartRateCard // Componente de UI para o card de frequência cardíaca.
import com.example.projeto_ttc2.presentation.ui.components.SleepCard // Componente de UI para o card de sono.
import com.example.projeto_ttc2.presentation.ui.components.StepsCard // Componente de UI para o card de passos.

// Habilita o uso de APIs experimentais do Material 3, como o PullToRefreshBox.
@OptIn(ExperimentalMaterial3Api::class)
// Define a função Composable, que representa a tela do dashboard supervisionado.
@Composable
fun SupervisedDashboardScreen(
    navController: NavController, // Parâmetro para gerenciar a navegação.
    userName: String, // Parâmetro para o nome do usuário a ser exibido.
    dashboardData: DashboardData, // Parâmetro com os dados a serem exibidos nos cards.
    onSosClick: () -> Unit, // Função a ser chamada ao clicar no botão SOS (não usada neste trecho).
    onLogout: () -> Unit, // Função a ser chamada para fazer logout.
    isRefreshing: Boolean, // Estado booleano que indica se a atualização está em andamento.
    onRefresh: () -> Unit // Função a ser chamada quando o usuário puxa para atualizar.
) {
    // Scaffold fornece a estrutura de layout principal da tela (como App Bar, conteúdo, etc.).
    Scaffold(
        // O modificador faz o Scaffold ocupar todo o tamanho da tela.
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // 'innerPadding' é o espaço que o Scaffold reserva para barras (como a status bar).

        // PullToRefreshBox é o container que ativa a funcionalidade de "puxar para atualizar".
        PullToRefreshBox(
            isRefreshing = isRefreshing, // Controla a exibição do indicador de progresso (a bolinha que gira).
            onRefresh = onRefresh, // A função que será executada quando o usuário puxar e soltar.
            // O modificador faz o Box preencher todo o espaço dentro do Scaffold.
            modifier = Modifier.fillMaxSize()
        ) {
            // LazyColumn cria uma lista rolável vertical que só renderiza os itens visíveis na tela.
            LazyColumn(
                // Define um preenchimento nas laterais (esquerda e direita) do conteúdo da lista.
                contentPadding = PaddingValues(horizontal = 16.dp),
                // O modificador faz a lista preencher todo o espaço disponível e aplica o padding do Scaffold.
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                // Adiciona um espaçamento vertical de 8.dp entre cada item da lista.
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Define o primeiro item da lista.
                item {
                    // Exibe o componente de cabeçalho com o nome do usuário e o botão de logout.
                    DashboardHeader(userName = userName, onLogout = onLogout)
                }
                // Define o segundo item da lista.
                item {
                    // Exibe o card de frequência cardíaca com os dados de BPM.
                    HeartRateCard(bpm = dashboardData.heartRate)
                }
                // Define o terceiro item da lista.
                item {
                    // Exibe o card de passos, mostrando os passos, a meta e a distância.
                    StepsCard(
                        steps = dashboardData.steps,
                        goal = dashboardData.stepsGoal,
                        distanceKm = dashboardData.distanceKm
                    )
                }
                // Define o quarto item da lista.
                item {
                    // Exibe o card de calorias, mostrando as calorias ativas e o total.
                    CaloriesCard(
                        activeKcal = dashboardData.activeCaloriesKcal,
                        totalKcal = dashboardData.caloriesKcal
                    )
                }
                // Define o quinto item da lista.
                item {
                    // Exibe o card de sono.
                    SleepCard(
                        sleepSession = dashboardData.sleepSession,
                        // Define a ação de clique para navegar para a tela de detalhes do sono.
                        onClick = { navController.navigate("sleep_screen") }
                    )
                }
            }
        }
    }
}