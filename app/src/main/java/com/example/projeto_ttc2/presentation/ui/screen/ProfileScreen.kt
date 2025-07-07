package com.example.projeto_ttc2.presentation.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.projeto_ttc2.presentation.state.ProfileState
import com.example.projeto_ttc2.presentation.state.UserRole
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileState: ProfileState,
    onSaveProfile: (String, String, LocalDate?, Uri?) -> Unit,
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
    var editedGender by remember(user?.gender) { mutableStateOf(user?.gender ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {


            val isLoading = profileState is ProfileState.Loading

            Button(
                onClick = {
                    onSaveProfile(editedFullName, editedGender, editedBirthDate, selectedImageUri)
                },
                modifier = Modifier.fillMaxWidth(),
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