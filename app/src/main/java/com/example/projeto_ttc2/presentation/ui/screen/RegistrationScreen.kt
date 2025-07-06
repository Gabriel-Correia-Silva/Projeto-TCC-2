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
    onRegister: (name: String, role: String, supervisorIds: List<String>?) -> Unit
) {
    var name by remember { mutableStateOf(user.displayName ?: "") }
    var selectedRole by remember { mutableStateOf("") }
    var supervisorIdsText by remember { mutableStateOf("") }
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

        if (selectedRole == "supervised") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = supervisorIdsText,
                onValueChange = { supervisorIdsText = it },
                label = { Text("ID(s) do(s) Supervisor(es)") },
                placeholder = { Text("Separe os IDs por vírgula") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val supervisorIdsList = if (selectedRole == "supervised") {
                    supervisorIdsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    null
                }
                onRegister(name, selectedRole, supervisorIdsList)
            },
            modifier = Modifier.fillMaxWidth(),

            enabled = name.isNotBlank() && selectedRole.isNotBlank() && (selectedRole != "supervised" || supervisorIdsText.isNotBlank())
        ) {
            Text("Finalizar Cadastro")
        }
    }
}