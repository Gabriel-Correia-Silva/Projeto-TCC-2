package com.example.projeto_ttc2

import android.app.AlertDialog
import android.os.Bundle
import android.service.quicksettings.Tile
import androidx.compose.material3.Icon
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projeto_ttc2.ui.theme.ProjetoTTC2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetoTTC2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Gabriel",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val showDialog = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login com o Google")
        Button(
            onClick = { showDialog.value = true },
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = "Google Icon",
                modifier = Modifier.size(40.dp)
            )
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Dialog Title") },
            text = { Text(text = "Dialog Text") },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProjetoTTC2Theme {
        Greeting("Android")
    }
}