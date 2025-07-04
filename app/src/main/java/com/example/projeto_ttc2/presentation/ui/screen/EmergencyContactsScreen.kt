package com.example.projeto_ttc2.presentation.ui.screen

import android.content.Intent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.projeto_ttc2.database.entities.EmergencyContact
import com.example.projeto_ttc2.presentation.viewmodel.EmergencyContactViewModel
import androidx.core.net.toUri

@Composable
fun EmergencyContactsScreen(viewModel: EmergencyContactViewModel) {
    val contacts by viewModel.allContacts.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Contato")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(contacts) { contact ->
                    ContactItem(
                        contact = contact,
                        onDelete = { viewModel.delete(contact) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddContactDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, phone, relationship ->
                viewModel.insert(EmergencyContact(name = name, phone = phone, relationship = relationship))
                showDialog = false
            }
        )
    }
}

@Composable
fun ContactItem(contact: EmergencyContact, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "${contact.relationship} - ${contact.phone}", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, "tel:${contact.phone}".toUri())
                    context.startActivity(intent)
                    // Aqui você pode adicionar a lógica para registrar a chamada no histórico
                }) {
                    Icon(Icons.Filled.Call, contentDescription = "Ligar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Contato") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefone") })
                OutlinedTextField(value = relationship, onValueChange = { relationship = it }, label = { Text("Parentesco") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, phone, relationship) }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}