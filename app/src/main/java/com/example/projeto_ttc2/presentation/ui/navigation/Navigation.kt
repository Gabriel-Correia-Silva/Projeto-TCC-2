package com.example.projeto_ttc2.presentation.ui.navigation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.state.AuthState
import com.example.projeto_ttc2.presentation.state.ProfileState
import com.example.projeto_ttc2.presentation.state.UiState
import com.example.projeto_ttc2.presentation.state.UserRole
import com.example.projeto_ttc2.presentation.ui.components.MainAppHeader
import com.example.projeto_ttc2.presentation.ui.screen.*
import com.example.projeto_ttc2.presentation.viewmodel.*
import kotlinx.coroutines.flow.MutableStateFlow
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

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val userRole by authViewModel.userRole.collectAsStateWithLifecycle()

    val userName = authViewModel.getCurrentUser()?.displayName?.split(" ")?.firstOrNull() ?: "Utilizador"

    Scaffold(
        topBar = {
            val routesWithHeader = listOf(
                "supervisor_dashboard", "supervised_dashboard", "settings_screen",
                "sleep_screen", "emergency_contacts_screen", "heart_rate_detail_screen",
                "steps_detail_screen", "profile_screen", "night_monitoring_screen",
                "professional_screen", "feedback_list_screen", "sensors_settings_screen"
            )
            val isDashboard = currentRoute in listOf("supervisor_dashboard", "supervised_dashboard")
            if (currentRoute in routesWithHeader) {
                MainAppHeader(
                    title = when (currentRoute) {
                        "supervisor_dashboard" -> "Olá, $userName"
                        "supervised_dashboard" -> "Olá, $userName"
                        "settings_screen" -> "Configurações"
                        "sleep_screen" -> "Sono"
                        "emergency_contacts_screen" -> "Contatos de Emergência"
                        "heart_rate_detail_screen" -> "Frequência Cardíaca"
                        "steps_detail_screen" -> "Passos"
                        "profile_screen" -> "Perfil"
                        "night_monitoring_screen" -> "Monitoramento Noturno"
                        "professional_screen" -> "Profissional"
                        "feedback_list_screen" -> "Centro de Feedback"
                        "sensors_settings_screen" -> "Metas e Alertas"
                        else -> "App"
                    },
                    showBackArrow = !isDashboard,
                    onBackClick = { navController.popBackStack() },
                    showIcons = isDashboard,
                    onSettingsClick = { navController.navigate("settings_screen") },
                    onNotificationsClick = { navController.navigate("feedback_list_screen") },
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == "supervised_dashboard") {
                val primaryContact by emergencyContactViewModel.primaryContact.collectAsStateWithLifecycle()
                FloatingActionButton(
                    onClick = {
                        val contactToCall = primaryContact
                        if (contactToCall != null) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${contactToCall.phone}".toUri()
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
            startDestination = "initial_router",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("initial_router") {
                InitialRouter(
                    navController = navController,
                    authViewModel = authViewModel,
                    healthConnectViewModel = healthConnectViewModel
                )
            }

            composable("permission_screen") {
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    contract = PermissionController.createRequestPermissionResultContract()
                ) {
                    navController.navigate("initial_router") {
                        popUpTo("permission_screen") { inclusive = true }
                    }
                }
                PermissionScreen(onContinueClick = {
                    healthConnectViewModel.requestPermissions(requestPermissionLauncher)
                })
            }

            composable("login") {
                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated || authState is AuthState.NeedsRegistration) {
                        navController.navigate("initial_router") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                LoginScreen(
                    authState = authState,
                    onSignInRequested = googleSignInLauncher,
                    onErrorShown = { authViewModel.clearErrorState() }
                )
            }

            composable("registration") {
                val currentState = authState
                if (currentState is AuthState.NeedsRegistration) {
                    RegistrationScreen(
                        user = currentState.user,
                        onRegister = { name, role, supervisorId ->
                            authViewModel.registerUser(currentState.user, name, role, supervisorId)
                            navController.navigate("initial_router") {
                                popUpTo("registration") { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("supervisor_dashboard") {
                SupervisorDashboardScreen(navController = navController)
            }

            composable("supervised_dashboard") {
                val uiState by healthConnectViewModel.uiState
                val latestBpm by dashboardViewModel.latestHeartRate.collectAsStateWithLifecycle()
                val todayHeartRateData by dashboardViewModel.todayHeartRateData.collectAsStateWithLifecycle()
                val todaySteps by dashboardViewModel.todaySteps.collectAsStateWithLifecycle()
                val stepGoal by dashboardViewModel.stepGoal.collectAsStateWithLifecycle()
                val todayDistanceKm by dashboardViewModel.todayDistanceKm.collectAsStateWithLifecycle()
                val sleepSession by dashboardViewModel.latestSleepSession.collectAsStateWithLifecycle()
                val activeCalories by dashboardViewModel.todayActiveCalories.collectAsStateWithLifecycle()
                val totalCalories by dashboardViewModel.todayTotalCalories.collectAsStateWithLifecycle()
                val oxygenSaturation by dashboardViewModel.latestOxygenSaturation.collectAsStateWithLifecycle()

                SupervisedDashboardScreen(
                    userName = userName,
                    dashboardViewModel = dashboardViewModel,
                    emergencyContactViewModel = emergencyContactViewModel,
                    dashboardData = DashboardData(
                        heartRate = latestBpm, steps = todaySteps, stepsGoal = stepGoal.toInt(),
                        distanceKm = todayDistanceKm,
                        activeCaloriesKcal = activeCalories, caloriesKcal = totalCalories,
                        sleepSession = sleepSession,
                        oxygenSaturation = oxygenSaturation
                    ),
                    heartRateData = todayHeartRateData,
                    onSosClick = { navController.navigate("emergency_contacts_screen") },
                    isRefreshing = uiState == UiState.Loading,
                    onManualRefresh = { scope.launch { healthConnectViewModel.syncData(true) } },
                    onBackgroundRefresh = { scope.launch { healthConnectViewModel.syncData(false) } },
                    onNavigateToFeedback = { navController.navigate("feedback_list_screen") },
                    onNavigateToSleep = { navController.navigate("sleep_screen") },
                    onNavigateToHeartRate = { navController.navigate("heart_rate_detail_screen") },
                    onNavigateToSteps = { navController.navigate("steps_detail_screen") }
                )
            }

            composable("professional_screen") { ProfessionalScreen() }
            composable("patient_detail/{patientId}") { PatientDetailScreen() }
            composable("settings_screen") {
                SettingsScreen(
                    navController = navController,
                    userRole = userRole
                )
            }
            composable("sensors_settings_screen") { SensorsSettingsScreen() }
            composable("feedback_list_screen") { FeedbackListScreen() }
            composable("emergency_contacts_screen") { EmergencyContactsScreen(viewModel = emergencyContactViewModel) }

            composable("sleep_screen") {
                SleepScreen(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel
                )
            }

            composable("night_monitoring_screen") { NightMonitoringScreen(navController = navController) }

            composable("profile_screen") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()

                when (val state = profileState) {
                    is ProfileState.Loading, is ProfileState.Initial -> LoadingScreen()
                    is ProfileState.Error -> {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Erro: ${state.message}")
                            Button(onClick = { profileViewModel.loadUserProfile() }) { Text("Tentar Novamente") }
                        }
                    }
                    is ProfileState.Success, is ProfileState.UpdateSuccess -> {
                        val user = if (state is ProfileState.Success) state.user else null
                        if (user != null) {
                            ProfileScreen(
                                profileState = state,
                                onSaveProfile = { fullName, birthDate ->
                                    profileViewModel.saveProfile(fullName, birthDate)
                                },
                                onClearState = { profileViewModel.clearState() },
                                userId = user.id,
                                userRole = userRole
                            )
                        } else {
                            LaunchedEffect(Unit) { profileViewModel.loadUserProfile() }
                            LoadingScreen()
                        }
                    }
                }
            }

            composable("heart_rate_detail_screen") {
                val latestBpm by dashboardViewModel.latestHeartRate.collectAsStateWithLifecycle()
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
        }
    }
}

@Composable
fun InitialRouter(
    navController: NavController,
    authViewModel: AuthViewModel,
    healthConnectViewModel: HealthConnectViewModel
) {
    LaunchedEffect(key1 = Unit) {
        val currentUser = authViewModel.getCurrentUser()
        if (currentUser == null) {
            navController.navigate("login") { popUpTo("initial_router") { inclusive = true } }
            return@LaunchedEffect
        }

        val authStateResult = authViewModel.checkUserRegistrationBlocking(currentUser.uid)
        if (authStateResult is AuthState.NeedsRegistration) {
            (authViewModel.authState as MutableStateFlow).value = authStateResult
            navController.navigate("registration") { popUpTo("initial_router") { inclusive = true } }
            return@LaunchedEffect
        }

        val userRole = authViewModel.userRole.value
        when (userRole) {
            is UserRole.Supervisor -> {
                navController.navigate("supervisor_dashboard") {
                    popUpTo("initial_router") { inclusive = true }
                }
            }
            is UserRole.Supervised -> {
                val hasPermissions = healthConnectViewModel.hasAllPermissions()
                if (!hasPermissions) {
                    navController.navigate("permission_screen") {
                        popUpTo("initial_router") { inclusive = true }
                    }
                } else {
                    navController.navigate("supervised_dashboard") {
                        popUpTo("initial_router") { inclusive = true }
                    }
                }
            }
            else -> {
                navController.navigate("login") {
                    popUpTo("initial_router") { inclusive = true }
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
        Spacer(modifier = Modifier.height(16.dp))
        Text("A carregar...")
    }
}