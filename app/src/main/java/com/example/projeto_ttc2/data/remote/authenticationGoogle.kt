package com.example.projeto_ttc2.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.projeto_ttc2.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider



class AuthenticationGoogle(context: Context) {

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.SEU_CLIENT_ID))
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Retorna Intent para iniciar fluxo do Google Sign-In.
     */
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    /**
     * Extrai GoogleSignInAccount a partir da Intent retornada pela Activity de Sign-In.
     */
    fun getSignedInAccountFromIntent(data: Intent?): Task<GoogleSignInAccount> =
        GoogleSignIn.getSignedInAccountFromIntent(data)

    /**
     * Faz sign-out do Google.
     * @param onComplete ação a executar ao concluir.
     */
    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener { onComplete() }
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Autentica no Firebase com o idToken do Google.
     * Se for primeira vez, o Firebase cria o usuário automaticamente.
     *
     * @param idToken  Token obtido de GoogleSignInAccount.
     * @param onComplete Callback com o AuthResult (inclui `additionalUserInfo.isNewUser`).
     * @param onFailure Callback em caso de erro.
     */
    fun firebaseAuthWithGoogle(
        idToken: String,
        onComplete: (authResult: AuthResult) -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    // Extrai AuthResult
                    val authResult = task.result!!

                    // Agora sim você pode logar os detalhes
                    Log.i(
                        "LoginGoogle",
                        "AuthResult: user=${authResult.user?.email}, " +
                                "isNew=${authResult.additionalUserInfo?.isNewUser}"
                    )

                    // E chama o callback
                    onComplete(authResult)
                } else {
                    onFailure(task.exception)
                }
            }
    }

}
