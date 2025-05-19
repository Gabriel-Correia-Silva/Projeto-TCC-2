package com.example.projeto_ttc2.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontVariation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.projeto_ttc2.presentation.ui.screen.HomeScreenContainer
import com.example.projeto_ttc2.presentation.ui.screen.RegisterScreenContainer
import com.example.projeto_ttc2.presentation.ui.screen.LoginScreenContainer
import com.example.projeto_ttc2.presentation.ui.screens.SettingsScreen
import com.example.projeto_ttc2.presentation.ui.screens.StatsAndActivitiesScreen
import com.example.projeto_ttc2.presentation.viewmodel.AuthViewModel



object Routes{
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val STEPS =  "steps"

}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser = authViewModel.currentUser.collectAsState().value

    NavHost(
        navController = navController ,
        startDestination = if(currentUser != null) Routes.HOME else Routes.LOGIN
    ){
        composable(Routes.LOGIN) {
            LoginScreenContainer(navController)
        }
        composable(Routes.REGISTER) {
            RegisterScreenContainer(navController)
        }
        composable (Routes.HOME){
            HomeScreenContainer(navController)
        }
        composable(Routes.SETTINGS ) {
            SettingsScreen(navController)
        }
        composable(Routes.STEPS) {
            StatsAndActivitiesScreen(
                navController,
                totalSteps = 350,
                distanceKm = 0.8f,
                totalTime = 60.toString(), // exemplo: 60 minutos
                onShareClick = { /* ação de compartilhar */ },
                activities = emptyList(), // ou uma lista de atividades mock
                onPeriodChange = { /* ação de mudança de período */ }
            )
        }
    }

}