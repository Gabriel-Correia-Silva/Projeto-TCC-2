package com.example.projeto_ttc2.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.presentation.navigation.AppNavigation
import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme
import com.example.projeto_ttc2.presentation.viewmodel.AuthViewModel
import com.example.projeto_ttc2.presentation.viewmodel.HealthConnectViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val healthConnectViewModel: HealthConnectViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()


    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { authViewModel.signInWithGoogle(it) }
        } catch (e: ApiException) {

            Log.e("GoogleSignIn", "Falha no login com Google. Status Code: ${e.statusCode}, Mensagem: ${e.message}")
            authViewModel.setError("Falha no login com Google: ${e.statusCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjetoTTC2Theme {
                AppNavigation(
                    authViewModel = authViewModel,
                    healthConnectViewModel = healthConnectViewModel,
                    googleSignInLauncher = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val signInClient = GoogleSignIn.getClient(this, gso)
                        googleSignInLauncher.launch(signInClient.signInIntent)
                    }
                )
            }
        }
    }
}