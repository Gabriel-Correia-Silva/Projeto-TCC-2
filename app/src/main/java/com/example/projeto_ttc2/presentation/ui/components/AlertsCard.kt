// Define o pacote onde este componente está localizado.
package com.example.projeto_ttc2.presentation.ui.components

// Importa os componentes e funcionalidades necessárias do Jetpack Compose.
import androidx.compose.foundation.layout.Arrangement // Para organizar os itens com espaçamento.
import androidx.compose.foundation.layout.Column // Um layout que organiza os filhos verticalmente.
import androidx.compose.foundation.layout.Spacer // Um componente para criar um espaço vazio.
import androidx.compose.foundation.layout.height // Modificador para definir a altura de um componente.
import androidx.compose.material3.Text // Componente para exibir texto.
import androidx.compose.runtime.Composable // Anotação que marca uma função como um componente de UI do Compose.
import androidx.compose.ui.Modifier // Objeto para adicionar propriedades a um componente.
import androidx.compose.ui.graphics.Color // Classe para definir cores.
import androidx.compose.ui.text.font.FontWeight // Para definir o peso (negrito, normal, etc.) da fonte.
import androidx.compose.ui.unit.dp // Unidade de medida para especificar tamanhos.

// Define a função Composable, que representa o card de alertas.
@Composable
fun AlertsCard() {
    // Usa o componente reutilizável 'DashboardCard' como base.
    // Como 'onClick' agora é opcional no 'DashboardCard', não precisamos passá-lo aqui.
    DashboardCard {
        // Exibe o texto do título do card em branco e em negrito.
        Text("Meus Alertas Recentes", color = Color.White, fontWeight = FontWeight.Bold)
        // Adiciona um espaço vertical de 12.dp entre o título e a lista de alertas.
        Spacer(modifier = Modifier.height(12.dp))
        // Cria uma coluna para organizar os textos dos alertas verticalmente.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // Adiciona 8.dp de espaço entre cada alerta.
            // Exibe o texto do primeiro alerta.
            Text("• Alerta de queda detectado (05/02)", color = Color.White)
            // Exibe o texto do segundo alerta.
            Text("• Bateria fraca do sensor (04/02)", color = Color.White)
        }
    }
}