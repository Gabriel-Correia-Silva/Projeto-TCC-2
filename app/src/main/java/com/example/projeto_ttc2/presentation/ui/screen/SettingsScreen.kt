package com.example.projeto_ttc2.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.presentation.ui.components.FloatingCallButton
import com.example.projeto_ttc2.presentation.ui.navigation.Routes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    navController: NavHostController,
    onPerfilClick: () -> Unit = {},
    onMonitoramentoClick: () -> Unit = {},
    onEmergenciaClick: () -> Unit = {},
    onNotificacoesClick: () -> Unit = {},
    onSensoresClick: () -> Unit = {},
    onTemaClick: () -> Unit = {},
    onSairClick: () -> Unit = {},
    onChamadaEmergencia: () -> Unit = {}
) {
    val context = LocalContext.current

    val googleClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2))
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Título
            Text(
                text = "Configurações",
                fontSize = 20.sp,
                color = Color(0xFF007C91),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Botões
            SettingButton("Perfil", Icons.Default.Person, onPerfilClick)
            SettingButton("Monitoramento noturno", Icons.Default.Info, onMonitoramentoClick)
            SettingButton("Contatos de emergência", Icons.Default.Phone, onEmergenciaClick)
            SettingButton("Notificações", Icons.Default.Notifications, onNotificacoesClick)
            SettingButton("Sensores", Icons.Default.Settings, onSensoresClick)
            SettingButton("Tema", Icons.Default.Refresh, onTemaClick)
            SettingButton("Sair", Icons.Default.ExitToApp) {
                googleClient.signOut().addOnCompleteListener {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            }

        }

        FloatingCallButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            onClick = { /* lógica de chamada */ }
        )
    }
}


@Composable
private fun SettingButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF0097A7), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Abrir $label",
                tint = Color.White
            )
        }
    }
}
