package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch


private const val MIN_HEART_RATE = 100L
private const val MAX_HEART_RATE = 200L
private const val MIN_STEP_GOAL = 1000L
private const val MAX_STEP_GOAL = 50000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsSettingsScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val heartRateThreshold by viewModel.heartRateThreshold.collectAsStateWithLifecycle()
    val stepGoal by viewModel.stepGoal.collectAsStateWithLifecycle()

    var currentHeartRateThreshold by remember { mutableStateOf(heartRateThreshold.toString()) }
    var isHeartRateError by remember { mutableStateOf(false) }

    var currentStepGoal by remember { mutableStateOf(stepGoal.toString()) }
    var isStepGoalError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(heartRateThreshold) {
        currentHeartRateThreshold = heartRateThreshold.toString()
    }
    LaunchedEffect(stepGoal) {
        currentStepGoal = stepGoal.toString()
    }


    fun validateFields() {
        val hrValue = currentHeartRateThreshold.toLongOrNull()
        isHeartRateError = hrValue == null || hrValue !in MIN_HEART_RATE..MAX_HEART_RATE

        val sgValue = currentStepGoal.toLongOrNull()
        isStepGoalError = sgValue == null || sgValue !in MIN_STEP_GOAL..MAX_STEP_GOAL
    }

    LaunchedEffect(currentHeartRateThreshold, currentStepGoal) {
        validateFields()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Metas e Alertas",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = currentHeartRateThreshold,
                onValueChange = { currentHeartRateThreshold = it.filter { char -> char.isDigit() } },
                label = { Text("Alerta de Batimento Cardíaco (BPM)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isHeartRateError,
                supportingText = {
                    if (isHeartRateError) {
                        Text("O valor deve estar entre $MIN_HEART_RATE e $MAX_HEART_RATE.")
                    }
                }
            )

            OutlinedTextField(
                value = currentStepGoal,
                onValueChange = { currentStepGoal = it.filter { char -> char.isDigit() } },
                label = { Text("Meta de Passos Diária") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isStepGoalError,
                supportingText = {
                    if (isStepGoalError) {
                        Text("O valor deve estar entre $MIN_STEP_GOAL e $MAX_STEP_GOAL.")
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.setHeartRateThreshold(currentHeartRateThreshold.toLong())
                    viewModel.setStepGoal(currentStepGoal.toLong())

                    scope.launch {
                        snackbarHostState.showSnackbar("Metas salvas com sucesso!")
                    }
                },
                enabled = !isHeartRateError && !isStepGoalError,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Salvar")
            }
        }
    }
}