package com.example.projeto_ttc2.presentation.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.state.AuthState
import com.example.projeto_ttc2.presentation.state.UiState
import com.example.projeto_ttc2.presentation.state.UserRole
import com.example.projeto_ttc2.presentation.ui.components.MainAppHeader
import com.example.projeto_ttc2.presentation.ui.screen.*
import com.example.projeto_ttc2.presentation.viewmodel.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    healthConnectViewModel: HealthConnectViewModel,
    dashboardViewModel: DashboardViewModel,
    googleSignInLauncher: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Inicializa o HealthConnectViewModel uma vez
    LaunchedEffect(Unit) {
        healthConnectViewModel.initialLoad(context)
    }

    val routesWithHeader = listOf("supervisor_dashboard", "supervised_dashboard", "settings_screen", "sleep_screen")

    fun getTitleForRoute(route: String?, userName: String): String {
        return when (route) {
            "supervisor_dashboard" -> "Olá, $userName"
            "supervised_dashboard" -> "Olá, $userName"
            "settings_screen" -> "Configurações"
            "sleep_screen" -> "Sono"
            else -> "App"
        }
    }

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val userRole by authViewModel.userRole.collectAsStateWithLifecycle()
    val uiState by healthConnectViewModel.uiState
    val latestBpm by dashboardViewModel.latestHeartRate.collectAsStateWithLifecycle()
    val todayHeartRateData by dashboardViewModel.todayHeartRateData.collectAsStateWithLifecycle()
    val todaySteps by dashboardViewModel.todaySteps.collectAsStateWithLifecycle()
    val todayDistanceKm by dashboardViewModel.todayDistanceKm.collectAsStateWithLifecycle()
    val sleepSession by dashboardViewModel.latestSleepSession.collectAsStateWithLifecycle()
    val activeCalories by dashboardViewModel.todayActiveCalories.collectAsStateWithLifecycle()
    val totalCalories by dashboardViewModel.todayTotalCalories.collectAsStateWithLifecycle()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        scope.launch {
            if (granted.containsAll(healthConnectViewModel.permissions)) {
                healthConnectViewModel.onPermissionsResult(granted)
                navController.navigate("login") {
                    popUpTo("splash_screen") { inclusive = true }
                }
            } else {
                healthConnectViewModel.onPermissionsResult(granted)
            }
        }
    }

    LaunchedEffect(authState, userRole) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                val destination = when (userRole) {
                    is UserRole.Supervisor -> "supervisor_dashboard"
                    is UserRole.Supervised -> "supervised_dashboard"
                    else -> null
                }
                if (destination != null && destination != currentRoute) {
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                        popUpTo("splash_screen") { inclusive = true }
                    }
                }
            }
            is AuthState.NeedsRegistration -> {
                if (currentRoute != "registration") {
                    navController.navigate("registration") {
                        popUpTo("login") { inclusive = true }
                        popUpTo("splash_screen") { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                // Não faz nada aqui para evitar navegações inesperadas
            }
            else -> {
                if (currentRoute !in listOf("login", "registration", "splash_screen")) {
                    navController.navigate("splash_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (currentRoute in routesWithHeader) {
                MainAppHeader(
                    title = getTitleForRoute(
                        currentRoute,
                        Firebase.auth.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Usuário"
                    ),
                    onSettingsClick = { navController.navigate("settings_screen") },
                    onNotificationsClick = { /* TODO: Navegar para notificações */ },
                    onLogout = { authViewModel.signOut() }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash_screen",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash_screen") {
                LaunchedEffect(uiState) {
                    if (healthConnectViewModel.hasAllPermissions()) {
                        navController.navigate("login") {
                            popUpTo("splash_screen") { inclusive = true }
                        }
                    }
                }
                SplashScreen(
                    onContinueClick = {
                        scope.launch {
                            if (healthConnectViewModel.hasAllPermissions()) {
                                navController.navigate("login") {
                                    popUpTo("splash_screen") { inclusive = true }
                                }
                            } else {
                                requestPermissionLauncher.launch(healthConnectViewModel.permissions)
                            }
                        }
                    }
                )
            }

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
                SupervisedDashboardScreen(
                    userName = Firebase.auth.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Usuário",
                    dashboardData = DashboardData(
                        heartRate = latestBpm,
                        steps = todaySteps,
                        distanceKm = todayDistanceKm,
                        sleepSession = sleepSession,
                        activeCaloriesKcal = activeCalories,
                        caloriesKcal = totalCalories
                    ),
                    heartRateData = todayHeartRateData,
                    onSosClick = { /* Lógica do botão SOS */ },
                    isRefreshing = uiState is UiState.Loading,
                    onRefresh = { healthConnectViewModel.syncData() },
                    onNavigateToSleep = { navController.navigate("sleep_screen") }
                )
            }

            composable("supervised_dashboard") {
                SupervisedDashboardScreen(
                    userName = Firebase.auth.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Usuário",
                    dashboardData = DashboardData(
                        heartRate = latestBpm,
                        steps = todaySteps,
                        distanceKm = todayDistanceKm,
                        sleepSession = sleepSession,
                        activeCaloriesKcal = activeCalories,
                        caloriesKcal = totalCalories
                    ),
                    heartRateData = todayHeartRateData,
                    onSosClick = { /* Lógica do botão SOS */ },
                    isRefreshing = uiState is UiState.Loading,
                    onRefresh = { healthConnectViewModel.syncData() },
                    onNavigateToSleep = { navController.navigate("sleep_screen") }
                )
            }
            composable("profile_screen") {
                ProfileScreen(
                    userName = Firebase.auth.currentUser?.displayName ?: "Usuário",
                    userEmail = Firebase.auth.currentUser?.email ?: "email@exemplo.com"
                    // Adicione outros parâmetros se necessário
                )
            }

            composable("settings_screen") {
                SettingsScreen(navController = navController)
            }

            composable("sleep_screen") {
                SleepScreen(navController = navController, sleepData = sleepSession)
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
}