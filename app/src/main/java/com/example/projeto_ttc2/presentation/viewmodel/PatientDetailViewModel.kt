package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.*
import com.example.projeto_ttc2.database.repository.FeedbackRepository
import com.example.projeto_ttc2.database.repository.FirebaseHealthDataRepository
import com.example.projeto_ttc2.database.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val healthDataRepository: FirebaseHealthDataRepository,
    private val feedbackRepository: FeedbackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    sealed class FeedbackState {
        object Idle : FeedbackState()
        object Loading : FeedbackState()
        object Success : FeedbackState()
        data class Error(val message: String) : FeedbackState()
    }

    private val patientId: String = savedStateHandle.get<String>("patientId")!!

    private val _patient = MutableStateFlow<User?>(null)
    val patient: StateFlow<User?> = _patient.asStateFlow()

    private val _stepsData = MutableStateFlow<List<Passos>>(emptyList())
    val stepsData: StateFlow<List<Passos>> = _stepsData.asStateFlow()

    private val _heartRateData = MutableStateFlow<List<BatimentoCardiaco>>(emptyList())
    val heartRateData: StateFlow<List<BatimentoCardiaco>> = _heartRateData.asStateFlow()

    private val _sleepData = MutableStateFlow<List<Sono>>(emptyList())
    val sleepData: StateFlow<List<Sono>> = _sleepData.asStateFlow()

    private val _caloriesData = MutableStateFlow<List<Calorias>>(emptyList())
    val caloriesData: StateFlow<List<Calorias>> = _caloriesData.asStateFlow()


    private val _feedbackState = MutableStateFlow<FeedbackState>(FeedbackState.Idle)
    val feedbackState: StateFlow<FeedbackState> = _feedbackState.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _patient.value = userRepository.getUser(patientId)
            healthDataRepository.getUserStepsData(patientId).collect { _stepsData.value = it }
            healthDataRepository.getAllHeartRateData(patientId).collect { _heartRateData.value = it }
            healthDataRepository.getUserSleepData(patientId).collect { _sleepData.value = it }
            healthDataRepository.getUserCaloriesData(patientId).collect { _caloriesData.value = it }
        }
    }

    fun sendFeedback(senderId: String, message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _feedbackState.value = FeedbackState.Loading
            try {
                val feedback = Feedback(
                    senderId = senderId,
                    recipientId = patientId,
                    message = message,
                    read = false
                )
                feedbackRepository.sendFeedback(feedback)
                _feedbackState.value = FeedbackState.Success
            } catch (e: Exception) {
                _feedbackState.value = FeedbackState.Error(e.message ?: "Erro desconhecido") 
            }
        }
    }


    fun resetFeedbackState() {
        _feedbackState.value = FeedbackState.Idle
    }
}