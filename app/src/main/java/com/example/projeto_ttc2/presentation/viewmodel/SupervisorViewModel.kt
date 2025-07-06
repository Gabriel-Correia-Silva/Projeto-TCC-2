package com.example.projeto_ttc2.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.User
import com.example.projeto_ttc2.database.repository.FirebaseHealthDataRepository
import com.example.projeto_ttc2.database.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthSummary(
    val steps: Long = 0L,
    val heartRate: Long = 0L,
    val sleep: Long = 0L, // Em minutos
    val calories: Double = 0.0
)

@HiltViewModel
class SupervisorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val firebaseHealthDataRepository: FirebaseHealthDataRepository
) : ViewModel() {

    // Adicionado um TAG para facilitar a filtragem dos logs
    private val TAG = "SupervisorViewModel_Logs"

    private val _supervisedUsers = MutableStateFlow<List<User>>(emptyList())
    val supervisedUsers: StateFlow<List<User>> = _supervisedUsers.asStateFlow()

    private val _healthData = MutableStateFlow<Map<String, HealthSummary>>(emptyMap())
    val healthData: StateFlow<Map<String, HealthSummary>> = _healthData.asStateFlow()

    init {
        Log.d(TAG, "--- ViewModel INICIADO ---")
        loadSupervisedUsers()
    }

    private fun loadSupervisedUsers() {
        viewModelScope.launch {
            val supervisorId = auth.currentUser?.uid
            if (supervisorId == null) {
                Log.e(TAG, "ID do Supervisor é NULO. Não é possível carregar pacientes.")
                return@launch
            }

            Log.d(TAG, "Buscando pacientes para o supervisor com ID: $supervisorId")
            userRepository.getSupervisedUsers(supervisorId)
                .catch { exception ->
                    Log.e(TAG, "ERRO ao buscar a lista de supervisionados.", exception)
                }
                .collect { users ->
                    Log.i(TAG, "LISTA DE PACIENTES RECEBIDA. Total: ${users.size} pacientes.")
                    _supervisedUsers.value = users
                    if (users.isEmpty()) {
                        Log.w(TAG, "A lista de pacientes está vazia.")
                    }
                    users.forEach { user ->
                        Log.d(TAG, "Iniciando busca de dados para o paciente: ${user.name} (ID: ${user.id})")
                        loadHealthDataForUser(user.id)
                    }
                }
        }
    }

    fun loadHealthDataForUser(userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "($userId) - Carregando dados de saúde do Firestore...")
            val heartRatesFlow = firebaseHealthDataRepository.getUserHeartRateData(userId)
            val stepsFlow = firebaseHealthDataRepository.getUserStepsData(userId)
            val sleepFlow = firebaseHealthDataRepository.getUserSleepData(userId)
            val caloriesFlow = firebaseHealthDataRepository.getUserCaloriesData(userId)

            combine(heartRatesFlow, stepsFlow, sleepFlow, caloriesFlow) { heartRates, steps, sleepSessions, calories ->

                Log.d(TAG, "($userId) - DADOS BRUTOS RECEBIDOS: " +
                        "Batimentos=${heartRates.size}, " +
                        "Passos=${steps.size}, " +
                        "Sono=${sleepSessions.size}, " +
                        "Calorias=${calories.size}")

                val summary = HealthSummary(
                    steps = steps.firstOrNull()?.contagem ?: 0L,
                    heartRate = heartRates.firstOrNull()?.bpm ?: 0L,
                    sleep = sleepSessions.firstOrNull()?.durationMinutes ?: 0L,
                    calories = calories.firstOrNull()?.kilocalorias ?: 0.0
                )

                Log.i(TAG, "($userId) - RESUMO DE SAÚDE CRIADO: $summary")

                summary
            }.collect { summary ->
                _healthData.value = _healthData.value + (userId to summary)
                Log.i(TAG, "($userId) - Mapa de dados de saúde do supervisor ATUALIZADO.")
            }
        }
    }
}