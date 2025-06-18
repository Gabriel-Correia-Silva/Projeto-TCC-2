package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FallDetectionDialog(
    showDialog: Boolean,
    initialCountdown: Int = 30,
    onDismiss: () -> Unit,
    onTimerFinished: () -> Unit
) {
    if (showDialog) {
        var timeLeft by remember { mutableStateOf(initialCountdown) }


        LaunchedEffect(key1 = timeLeft) {

            if (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            } else {
                onTimerFinished()
            }
        }

        AlertDialog(
            onDismissRequest = { /* Não faz nada para forçar a interação */ },
            title = {},
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta de Queda",
                        tint = Color.Red,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Detectamos a possibilidade de uma queda. Caso não aperte o botão em até $initialCountdown segundos, ligaremos para seu contato de emergência.",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "$timeLeft segundos...",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Não houve queda")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}


@Preview
@Composable
fun FallDetectionDialogPreview() {
    var show by remember { mutableStateOf(true) }
    if (show) {
        FallDetectionDialog(
            showDialog = true,
            onDismiss = { show = false },
            onTimerFinished = { show = false }
        )
    }
}