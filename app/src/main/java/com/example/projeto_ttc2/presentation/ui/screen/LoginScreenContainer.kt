package com.example.projeto_ttc2.presentation.ui.screen


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.projeto_ttc2.data.remote.AuthenticationGoogle
import com.example.projeto_ttc2.presentation.ui.navigation.Routes

import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme

import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreenContainer(navController: NavHostController) {
    val context = LocalContext.current
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val authGoogle = remember { AuthenticationGoogle(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher para tratar o retorno do login com Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = authGoogle.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val idToken = account.idToken
            if (idToken != null) {
                authGoogle.firebaseAuthWithGoogle(
                    idToken = idToken,
                    onComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onFailure = { ex ->
                        errorMessage = "Falha no login com Google: ${ex?.message}"
                    }
                )
            } else {
                errorMessage = "ID Token retornou nulo."
            }
        } catch (e: Exception) {
            errorMessage = "Erro ao recuperar conta Google: ${e.message}"
        }
    }

    ProjetoTTC2Theme {
        LoginInterface(
            email = email,
            password = password,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },

            // Login por e-mail e senha
            onEmailLoginClick = { enteredEmail, enteredPassword ->
                firebaseAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        } else {
                            errorMessage = "Erro no login: ${task.exception?.message}"
                        }
                    }
            },

            // Login com Google via launcher
            onGoogleLoginClick = {
                val signInIntent = authGoogle.getSignInIntent()
                googleSignInLauncher.launch(signInIntent)
            },

            onRegisterClick = {
                navController.navigate(Routes.REGISTER)
            }
        )

        // Exibe Toast em caso de erro
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }
}
