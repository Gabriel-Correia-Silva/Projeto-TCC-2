package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HeartRateAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alerta de Batimento Cardíaco") },
        text = { Text("Seu batimento cardíaco chegou a um ponto perigoso! Irei alertar seu médico e a emergência!") },
        icon = { Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = Color.Red) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ligar para Emergência")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Estou bem")
            }
        }
    )
}