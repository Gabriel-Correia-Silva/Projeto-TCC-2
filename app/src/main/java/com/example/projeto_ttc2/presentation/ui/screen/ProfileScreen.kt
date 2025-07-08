package com.example.projeto_ttc2.presentation.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.presentation.state.ProfileState
import com.example.projeto_ttc2.presentation.state.UserRole
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileState: ProfileState,
    onSaveProfile: (String, LocalDate?) -> Unit, // Assinatura simplificada
    onClearState: () -> Unit,
    userId: String,
    userRole: UserRole?
) {

    val user = if (profileState is ProfileState.Success) profileState.user else null

    var editedFullName by remember(user?.name) { mutableStateOf(user?.name ?: "") }
    val initialDate = remember(user?.birthDate) {
        if (user?.birthDate != null) {
            try {
                LocalDate.parse(user.birthDate, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: DateTimeParseException) {
                null
            }
        } else {
            null
        }
    }
    var editedBirthDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(profileState) {
        if (profileState is ProfileState.UpdateSuccess) {
            snackbarHostState.showSnackbar("Perfil salvo com sucesso!")
            onClearState()
        }
        if (profileState is ProfileState.Error) {
            snackbarHostState.showSnackbar(profileState.message)
            onClearState()
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editedBirthDate?.atStartOfDay(TimeZone.getDefault().toZoneId())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        editedBirthDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Editar Perfil",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Campo de Nome
            OutlinedTextField(
                value = editedFullName,
                onValueChange = { editedFullName = it },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo de Data de Nascimento
            OutlinedTextField(
                value = editedBirthDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Selecione a data",
                onValueChange = {},
                readOnly = true,
                label = { Text("Data de Nascimento") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar Data")
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            val isLoading = profileState is ProfileState.Loading
            Button(
                onClick = {
                    // Chamada simplificada
                    onSaveProfile(editedFullName, editedBirthDate)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Perfil", fontSize = 16.sp)
                }
            }
        }
    }
}