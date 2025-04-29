package com.example.projeto_ttc2.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.projeto_ttc2.source.remote.AuthenticationGoogle
import com.example.projeto_ttc2.ui.home.HomeActivity
import com.example.projeto_ttc2.ui.login.LoginScreenActivity
import com.example.projeto_ttc2.ui.login.theme.ProjetoTTC2Theme
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginScreenActivity : ComponentActivity() {

    private lateinit var authGoogle: AuthenticationGoogle
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authGoogle = AuthenticationGoogle(this)
        firebaseAuth = FirebaseAuth.getInstance()

        // Launcher para resultado do Google Sign-In
        val googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = authGoogle.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val idToken = task.result?.idToken
                    if (idToken != null) {
                        authGoogle.firebaseAuthWithGoogle(
                            idToken,
                            onComplete = { authResult: AuthResult ->
                                handlePostGoogleSignIn(authResult)
                            },
                            onFailure = { ex ->
                                showError("Autenticação falhou: ${ex?.message}")
                            }
                        )
                    } else {
                        showError("ID Token nulo")
                    }
                } else {
                    showError("Google Sign-In falhou: ${task.exception?.message}")
                }
            } else {
                showError("Login Google cancelado")
            }
        }

        setContent {
            ProjetoTTC2Theme {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                LoginInterface(
                    email = email,
                    password = password,
                    onEmailChange = { newEmail -> email = newEmail },
                    onPasswordChange = { newPassword -> password = newPassword },
                    onEmailLoginClick = { enteredEmail, enteredPassword ->
                        firebaseAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navigateToHome(firebaseAuth.currentUser)
                                } else {
                                    showError("Login E-mail falhou: ${task.exception?.message}")
                                }
                            }
                    },
                    onGoogleLoginClick = {
                        Log.i("LoginScreen", "Botão de login com Google clicado")
                        val intent = authGoogle.getSignInIntent()
                        googleLauncher.launch(intent)
                    }

                )
            }
        }
    }

    /**
     * Tratamento pós-login Google: cadastra perfil se usuário novo.
     */
    private fun handlePostGoogleSignIn(authResult: AuthResult) {
        val user: FirebaseUser = authResult.user!!
        val isNew = authResult.additionalUserInfo?.isNewUser ?: false

        if (isNew) {
            // Exemplo: inicializar perfil no Firestore
            // Firestore
            //    .collection("users")
            //    .document(user.uid)
            //    .set(mapOf("name" to user.displayName, "email" to user.email))
        }

        navigateToHome(user)
    }

    /**
     * Navega para a tela principal (chamar HomeActivity).
     */
    private fun navigateToHome(user: FirebaseUser?) {
        lifecycleScope.launch {
            delay(1000) // espera 1 segundo (1000 ms)
            val intent = Intent(this@LoginScreenActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Exibe mensagem rápida via Toast.
     */
    private fun showError(message: String) {
        Log.e("LoginScreen", message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
