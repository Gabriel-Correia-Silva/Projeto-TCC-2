package com.example.projeto_ttc2.presentation.ui.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat // Ícone para o chat/profissional
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.state.AuthState
import com.example.projeto_ttc2.presentation.state.UiState
import com.example.projeto_ttc2.presentation.state.UserRole
import com.example.projeto_ttc2.presentation.ui.components.MainAppHeader
import com.example.projeto_ttc2.presentation.ui.screen.*
import com.example.projeto_ttc2.presentation.viewmodel.AuthViewModel
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import com.example.projeto_ttc2.presentation.viewmodel.EmergencyContactViewModel
import com.example.projeto_ttc2.presentation.viewmodel.HealthConnectViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    healthConnectViewModel: HealthConnectViewModel,
    dashboardViewModel: DashboardViewModel,
    emergencyContactViewModel: EmergencyContactViewModel,
    googleSignInLauncher: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        healthConnectViewModel.initialLoad(context)
    }

    val routesWithHeader = listOf(
        "supervisor_dashboard",
        "supervised_dashboard",
        "settings_screen",
        "sleep_screen",
        "emergency_contacts_screen",
        "heart_rate_detail_screen",
        "steps_detail_screen",
        "profile_screen",
        "night_monitoring_screen",
        "professional_screen" // Adicionada nova rota
    )
    fun getTitleForRoute(route: String?, userName: String): String {
        return when (route) {
            "supervisor_dashboard" -> "Olá, $userName" // Título para supervisor
            "supervised_dashboard" -> "Olá, $userName" // Título para supervisionado
            "settings_screen" -> "Configurações"
            "sleep_screen" -> "Sono"
            "emergency_contacts_screen" -> "Contatos de Emergência"
            "heart_rate_detail_screen" -> "Frequência Cardíaca"
            "steps_detail_screen" -> "Passos"
            "profile_screen" -> "Perfil"
            "night_monitoring_screen" -> "Monitoramento Noturno"
            "professional_screen" -> "Profissional" // Título para a nova tela
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

    val userName = Firebase.auth.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Usuário"

    LaunchedEffect(authState, userRole) {
        Log.d("AppNavigation", "Auth state changed. Auth: $authState, Role: $userRole")
        when (authState) {
            is AuthState.Authenticated -> {
                val destination = when (userRole) {
                    is UserRole.Supervisor -> "supervisor_dashboard"
                    is UserRole.Supervised -> "supervised_dashboard"
                    else -> null
                }
                if (destination != null && currentRoute != destination) {
                    Log.d("AppNavigation", "Navigating to dashboard: $destination")
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            is AuthState.NeedsRegistration -> {
                if (currentRoute != "registration") {
                    Log.d("AppNavigation", "Navigating to registration")
                    navController.navigate("registration") {
                        popUpTo("login") { inclusive = true }
                        popUpTo("splash_screen") { inclusive = true }
                    }
                }
            }
            is AuthState.Idle, is AuthState.Error -> {
                val protectedRoutes = routesWithHeader + listOf("profile_screen", "sleep_screen")
                if (currentRoute in protectedRoutes) {
                    Log.d("AppNavigation", "User not authenticated on a protected route. Navigating to login.")
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Loading -> {
                Log.d("AppNavigation", "Auth state is loading, waiting...")
            }
        }
    }
    val primaryContact by emergencyContactViewModel.primaryContact.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            if (currentRoute in routesWithHeader) {
                val isDashboard = currentRoute in listOf("supervisor_dashboard", "supervised_dashboard")
                MainAppHeader(
                    title = getTitleForRoute(currentRoute, userName),
                    showBackArrow = !isDashboard,
                    onBackClick = { navController.popBackStack() },
                    showIcons = isDashboard,
                    onSettingsClick = { navController.navigate("settings_screen") },
                    // Alterado para navegar para a tela profissional
                    onNotificationsClick = { navController.navigate("professional_screen") },
                    onLogout = { authViewModel.signOut() }
                )
            }
        },
        floatingActionButton = {
            val routesWithoutFab = listOf(
                "splash_screen",
                "login",
                "registration",
                "permission_screen"
            )
            if (currentRoute !in routesWithoutFab) {
                FloatingActionButton(
                    onClick = {
                        val contactToCall = primaryContact
                        if (contactToCall != null) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contactToCall.phone}")
                            }
                            context.startActivity(intent)
                        } else {
                            navController.navigate("emergency_contacts_screen")
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Ligação de emergência",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash_screen",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("splash_screen") {
                var hasNavigated by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    if (!hasNavigated) {
                        hasNavigated = true
                        val currentUser = Firebase.auth.currentUser
                        if (currentUser != null) {
                            authViewModel.checkUserRegistration(currentUser.uid)
                        } else {
                            navController.navigate("login") {
                                popUpTo("splash_screen") { inclusive = true }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Carregando...")
                }
            }

            composable("permission_screen") {
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    contract = PermissionController.createRequestPermissionResultContract(),
                    onResult = { permissions ->
                        scope.launch {
                            healthConnectViewModel.onPermissionsResult(permissions)
                            when (userRole) {
                                is UserRole.Supervisor -> navController.navigate("supervisor_dashboard") {
                                    popUpTo("permission_screen") { inclusive = true }
                                }
                                is UserRole.Supervised -> navController.navigate("supervised_dashboard") {
                                    popUpTo("permission_screen") { inclusive = true }
                                }
                                else -> {
                                    navController.navigate("login") {
                                        popUpTo("permission_screen") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                )

                PermissionScreen(
                    onContinueClick = {
                        scope.launch {
                            healthConnectViewModel.requestPermissions(requestPermissionLauncher)
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
                }
            }

            // ROTA DO SUPERVISOR ATUALIZADA
            composable("supervisor_dashboard") {
                SupervisorDashboardScreen() // Chama a nova tela do supervisor
            }

            // ROTA DO SUPERVISIONADO (Paciente)
            composable("supervised_dashboard") {
                SupervisedDashboardScreen(
                    userName = userName,
                    dashboardData = DashboardData(
                        heartRate = latestBpm,
                        steps = todaySteps,
                        distanceKm = todayDistanceKm,
                        activeCaloriesKcal = activeCalories,
                        caloriesKcal = totalCalories,
                        sleepSession = sleepSession
                    ),
                    heartRateData = todayHeartRateData,
                    onSosClick = { navController.navigate("emergency_contacts_screen") },
                    isRefreshing = uiState == UiState.Loading,
                    onManualRefresh = { scope.launch { healthConnectViewModel.syncData() } },
                    onBackgroundRefresh = { scope.launch { healthConnectViewModel.syncData(showIndicator = false) } },
                    onNavigateToSleep = { navController.navigate("sleep_screen") },
                    onNavigateToHeartRate = { navController.navigate("heart_rate_detail_screen") },
                    onNavigateToSteps = { navController.navigate("steps_detail_screen") }
                )
            }

            // NOVA ROTA PARA A TELA PROFISSIONAL
            composable("professional_screen") {
                ProfessionalScreen()
            }

            composable("profile_screen") {
                val currentUser = Firebase.auth.currentUser
                ProfileScreen(
                    userName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Usuário",
                    userEmail = currentUser?.email ?: "",
                    fullName = currentUser?.displayName ?: "",
                    onSaveProfile = { name, email, birthDate, gender, imageUri ->
                        // TODO: Implementar salvamento do perfil
                    }
                )
            }

            composable("settings_screen") {
                SettingsScreen(navController = navController)
            }

            composable("emergency_contacts_screen") {
                EmergencyContactsScreen(viewModel = emergencyContactViewModel)
            }

            composable("sleep_screen") {
                SleepScreen(navController = navController, sleepData = sleepSession)
            }



            composable("heart_rate_detail_screen") {
                HeartRateDetailScreen(
                    currentBpm = latestBpm,
                    dashboardViewModel = dashboardViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("steps_detail_screen") {
                StepsDetailScreen(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel,
                )
            }

            composable("night_monitoring_screen") {
                NightMonitoringScreen(navController = navController)
            }
        }
    }
}