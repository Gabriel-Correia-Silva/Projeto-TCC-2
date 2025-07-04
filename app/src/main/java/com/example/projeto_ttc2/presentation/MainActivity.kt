package com.example.projeto_ttc2.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.projeto_ttc2.presentation.ui.navigation.AppNavigation
import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme
import com.example.projeto_ttc2.presentation.viewmodel.*
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
            }
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