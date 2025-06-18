package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun RegistrationScreen(
    user: FirebaseUser,
    onRegister: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(user.displayName ?: "") }
    var role by remember { mutableStateOf("") }
    var supervisorId by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Complete seu cadastro", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Selecione seu papel:")

        Row {
            RadioButton(
                selected = role == "supervisor",
                onClick = { role = "supervisor" }
            )
            Text("Supervisor", modifier = Modifier.padding(end = 16.dp))
            RadioButton(
                selected = role == "supervised",
                onClick = { role = "supervised" }
            )
            Text("Supervisionado")
        }

        if (role == "supervised") {
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = supervisorId,
                onValueChange = { supervisorId = it },
                label = { Text("ID do Supervisor") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onRegister(name, role, if (role == "supervised") supervisorId else null)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && role.isNotBlank() && (role != "supervised" || supervisorId.isNotBlank())
        ) {
            Text("Cadastrar")
        }
    }
}