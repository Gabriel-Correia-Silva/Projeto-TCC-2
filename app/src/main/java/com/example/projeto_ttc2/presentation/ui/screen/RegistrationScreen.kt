package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun RegistrationScreen(
    user: FirebaseUser,
    onRegister: (name: String, role: String, supervisorId: String?) -> Unit
) {
    var name by remember { mutableStateOf(user.displayName ?: "") }
    var selectedRole by remember { mutableStateOf("") }
    var supervisorId by remember { mutableStateOf("") }
    val roles = listOf("supervisor", "supervised")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Complete seu Cadastro", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Qual é a sua função?", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Seleção de Função (Role)
        Column {
            roles.forEach { role ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (selectedRole == role),
                            onClick = { selectedRole = role }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedRole == role),
                        onClick = { selectedRole = role }
                    )
                    Text(
                        text = if (role == "supervisor") "Supervisor" else "Supervisionado",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Campo de ID do Supervisor (só aparece se 'supervised' for selecionado)
        if (selectedRole == "supervised") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = supervisorId,
                onValueChange = { supervisorId = it },
                label = { Text("ID do Supervisor") },
                placeholder = { Text("Peça o ID ao seu supervisor") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Empurra o botão para baixo

        Button(
            onClick = {
                onRegister(name, selectedRole, if (selectedRole == "supervised") supervisorId else null)
            },
            modifier = Modifier.fillMaxWidth(),
            // O botão só fica ativo se todas as condições forem atendidas
            enabled = name.isNotBlank() && selectedRole.isNotBlank() && (selectedRole != "supervised" || supervisorId.isNotBlank())
        ) {
            Text("Finalizar Cadastro")
        }
    }
}