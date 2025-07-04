package com.example.projeto_ttc2.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.projeto_ttc2.presentation.navigation.AppNavigation
import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme
import com.example.projeto_ttc2.presentation.viewmodel.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
                authViewModel.handleSignInResult(account)
            }
        } catch (e: ApiException) {
            // authViewModel.handleSignInResult(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectViewModel.initialize(this)
        setContent {
            ProjetoTTC2Theme {
                AppNavigation(
                    authViewModel = authViewModel,
                    healthConnectViewModel = healthConnectViewModel,
                    dashboardViewModel = dashboardViewModel,
                    emergencyContactViewModel = emergencyContactViewModel,

                    googleSignInLauncher = {
                        val signInIntent = authViewModel.getGoogleSignInClient(this).signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }
                )
            }
        }
    }
}