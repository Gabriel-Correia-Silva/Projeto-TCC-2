package com.example.projeto_ttc2.presentation.navigation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.state.AuthState
import com.example.projeto_ttc2.presentation.state.UiState
import com.example.projeto_ttc2.presentation.state.UserRole
import com.example.projeto_ttc2.presentation.ui.screen.LoginScreen
import com.example.projeto_ttc2.presentation.ui.screen.RegistrationScreen
import com.example.projeto_ttc2.presentation.ui.screen.SupervisedDashboardScreen
import com.example.projeto_ttc2.presentation.viewmodel.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    healthConnectViewModel: HealthConnectViewModel,
    googleSignInLauncher: () -> Unit
) {
    val navController = rememberNavController()


    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val userRole by authViewModel.userRole.collectAsStateWithLifecycle()
    val uiState by healthConnectViewModel.uiState
    val latestBpm by healthConnectViewModel.latestHeartRate.collectAsStateWithLifecycle(initialValue = 0L)
    val todaySteps by healthConnectViewModel.todaySteps.collectAsStateWithLifecycle()
    val todayDistanceKm by healthConnectViewModel.todayDistanceKm.collectAsStateWithLifecycle()
    val sleepSession by healthConnectViewModel.latestSleepSession.collectAsStateWithLifecycle()

    LaunchedEffect(authState, userRole) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                val destination = when (userRole) {
                    is UserRole.Supervisor -> "supervisor_dashboard"
                    is UserRole.Supervised -> "supervised_dashboard"
                    else -> null
                }
                destination?.let {
                    navController.navigate(it) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            is AuthState.NeedsRegistration -> {
                navController.navigate("registration") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> {

            }
            else -> { // Unauthenticated
                if (navController.currentBackStackEntry?.destination?.route != "login") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }


    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onSignInRequested = googleSignInLauncher)
        }

        composable("registration") {
            val currentState = authState
            if (currentState is AuthState.NeedsRegistration) {
                RegistrationScreen(
                    user = currentState.user,
                    onRegister = { name, role, supervisorId ->
                        authViewModel.registerUser(currentState.user, name, role, supervisorId)
                    }
                )
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable("supervisor_dashboard") {
            Text("Supervisor Dashboard - Logado!")
        }

        composable("supervised_dashboard") {
            val context = LocalContext.current


            LaunchedEffect(Unit) {
                healthConnectViewModel.syncData()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val currentUser = Firebase.auth.currentUser
                SupervisedDashboardScreen(
                    userName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Usuário",

                    dashboardData = DashboardData(
                        heartRate = latestBpm,
                        steps = todaySteps,
                        distanceKm = todayDistanceKm,
                        sleepSession = sleepSession
                    ),
                    onSosClick = { /* Lógica do botao SOS */ },
                    onLogout = { authViewModel.signOut() },
                    isRefreshing = uiState is UiState.Loading,
                    onRefresh = { healthConnectViewModel.syncData() }
                )

                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is UiState.Error -> {
                        Text(
                            text = "Erro: ${state.message}",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {}
                }
            }
        }

        composable(
            "error/{message}",
            arguments = listOf(navArgument("message") { type = NavType.StringType })
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: "Ocorreu um erro."
            Text("Erro: $message")
        }
    }
}