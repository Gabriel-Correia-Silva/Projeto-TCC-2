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
    val sleep: Long = 0L,
    val calories: Double = 0.0
)

@HiltViewModel
class SupervisorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val firebaseHealthDataRepository: FirebaseHealthDataRepository
) : ViewModel() {

    private val _supervisedUsers = MutableStateFlow<List<User>>(emptyList())
    val supervisedUsers: StateFlow<List<User>> = _supervisedUsers.asStateFlow()

    // Mapa: userId -> HealthSummary
    private val _healthData = MutableStateFlow<Map<String, HealthSummary>>(emptyMap())
    val healthData: StateFlow<Map<String, HealthSummary>> = _healthData.asStateFlow()

    init {
        loadSupervisedUsers()
    }

    private fun loadSupervisedUsers() {
        viewModelScope.launch {
            val supervisorId = auth.currentUser?.uid
            if (supervisorId != null) {
                userRepository.getSupervisedUsers(supervisorId)
                    .catch { exception ->
                        Log.e("SupervisorViewModel", "Erro no Flow", exception)
                    }
                    .collect { users ->
                        Log.d("SupervisorViewModel", "ViewModel recebeu ${users.size} usuários.")
                        _supervisedUsers.value = users
                        users.forEach { user ->
                            loadHealthDataForUser(user.id)
                        }
                    }
            }
        }
    }

    fun loadHealthDataForUser(userId: String) {
        viewModelScope.launch {
            // Busca os dados de saúde do Firestore para o usuário
            val heartRatesFlow = firebaseHealthDataRepository.getUserHeartRateData(userId)
            val stepsFlow = firebaseHealthDataRepository.getUserStepsData(userId)
            val sleepFlow = firebaseHealthDataRepository.getUserSleepData(userId)
            val caloriesFlow = firebaseHealthDataRepository.getUserCaloriesData(userId)

            // Coleta todos os dados e atualiza o mapa
            heartRatesFlow.combine(stepsFlow) { hrList, stepsList ->
                Pair(hrList, stepsList)
            }.combine(sleepFlow) { (hrList, stepsList), sleepList ->
                Triple(hrList, stepsList, sleepList)
            }.combine(caloriesFlow) { (hrList, stepsList, sleepList), calList ->
                HealthSummary(
                    steps = stepsList.lastOrNull()?.contagem ?: 0L,
                    heartRate = hrList.lastOrNull()?.bpm ?: 0L,
                    sleep = sleepList.lastOrNull()?.durationMinutes ?: 0L,
                    calories = calList.lastOrNull()?.kilocalorias ?: 0.0
                )
            }.collect { summary ->
                _healthData.value = _healthData.value + (userId to summary)
            }
        }
    }
}