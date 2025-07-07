package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.User
import com.example.projeto_ttc2.database.repository.AuthRepository
import com.example.projeto_ttc2.database.repository.FirebaseHealthDataRepository
import com.example.projeto_ttc2.database.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthSummary(
    val steps: Long = 0,
    val heartRate: Long = 0,
    val sleep: Long = 0,
    val calories: Double = 0.0
)

@HiltViewModel
class SupervisorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val healthDataRepository: FirebaseHealthDataRepository
) : ViewModel() {


    private val _supervisorId = MutableStateFlow<String?>(null)


    val supervisedUsers: StateFlow<List<User>> = _supervisorId.flatMapLatest { id ->

        userRepository.getSupervisedUsers(id ?: "")
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())


    val healthData: StateFlow<Map<String, HealthSummary>> = supervisedUsers.flatMapLatest { users ->
        val userHealthFlows = users.map { user ->

            combine(
                healthDataRepository.getUserStepsData(user.id),
                healthDataRepository.getUserHeartRateData(user.id),
                healthDataRepository.getUserSleepData(user.id),
                healthDataRepository.getUserCaloriesData(user.id)
            ) { stepsData, heartRateData, sleepData, caloriesData ->

                user.id to HealthSummary(
                    steps = stepsData.firstOrNull()?.contagem ?: 0L,
                    heartRate = heartRateData.firstOrNull()?.bpm ?: 0L,
                    sleep = sleepData.firstOrNull()?.durationMinutes ?: 0L,
                    calories = caloriesData.firstOrNull()?.kilocalorias ?: 0.0
                )
            }
        }


        combine(userHealthFlows) { summaries ->
            summaries.toMap()
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyMap())


    init {
        viewModelScope.launch {
            _supervisorId.value = authRepository.getCurrentUser()?.uid
        }
    }
}