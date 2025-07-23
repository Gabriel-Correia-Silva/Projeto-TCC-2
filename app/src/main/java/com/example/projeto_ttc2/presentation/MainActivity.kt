package com.example.projeto_ttc2.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.projeto_ttc2.presentation.ui.components.FallDetectionDialog
import com.example.projeto_ttc2.presentation.ui.navigation.AppNavigation
import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme
import com.example.projeto_ttc2.presentation.viewmodel.AuthViewModel
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import com.example.projeto_ttc2.presentation.viewmodel.EmergencyContactViewModel
import com.example.projeto_ttc2.presentation.viewmodel.HealthConnectViewModel
import com.example.projeto_ttc2.presentation.viewmodel.ProfileViewModel

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val healthConnectViewModel: HealthConnectViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val emergencyContactViewModel: EmergencyContactViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private var showFallDialog by mutableStateOf(false)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                account.idToken?.let { idToken ->
                    authViewModel.signInWithGoogle(idToken)
                } ?: run {
                    authViewModel.setError("Token de ID não encontrado")
                }
            }
        } catch (e: ApiException) {
            authViewModel.setError("Falha no login: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectViewModel.initialLoad(this)
        handleIntent(intent)

        setContent {
            ProjetoTTC2Theme {
                AppNavigation(
                    authViewModel = authViewModel,
                    healthConnectViewModel = healthConnectViewModel,
                    dashboardViewModel = dashboardViewModel,
                    emergencyContactViewModel = emergencyContactViewModel,
                    googleSignInLauncher = {
                        val signInIntent = getGoogleSignInClient().signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }
                )

                FallDetectionDialog(
                    showDialog = showFallDialog,
                    onDismiss = { showFallDialog = false },
                    onTimerFinished = {
                        showFallDialog = false
                        dashboardViewModel.triggerEmergencyActions(this, emergencyContactViewModel.primaryContact.value?.phone)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("SHOW_FALL_DIALOG", false) == true) {
            showFallDialog = true
            intent.removeExtra("SHOW_FALL_DIALOG")
        }
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.projeto_ttc2.R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }


}
