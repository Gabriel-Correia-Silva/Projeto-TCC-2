package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.repository.CaloriesRepository
import com.example.projeto_ttc2.database.repository.HeartRateRepository
import com.example.projeto_ttc2.database.repository.SleepRepository
import com.example.projeto_ttc2.database.repository.StepsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    // Injeta apenas os repositórios de DADOS que o Dashboard precisa
    heartRateRepository: HeartRateRepository,
    stepsRepository: StepsRepository,
    sleepRepository: SleepRepository,
    caloriesRepository: CaloriesRepository
) : ViewModel() {

    // --- OS STATEFLOWS DE DADOS AGORA VIVEM AQUI ---

    val latestHeartRate: StateFlow<Long> = heartRateRepository.getLatestHeartRate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayHeartRateData: StateFlow<List<Long>> = heartRateRepository.getTodayHeartRateData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHeartRateRecords = heartRateRepository.getTodayHeartRateRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySteps: StateFlow<Long> = stepsRepository.getTodayStepsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val latestSleepSession: StateFlow<Sono?> = sleepRepository.getLatestSleepSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayActiveCalories: StateFlow<Double> = caloriesRepository.getTodayActiveCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayTotalCalories: StateFlow<Double> = caloriesRepository.getTodayTotalCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // A lógica de dados derivados (como distância) também pertence a este ViewModel
    val todayDistanceKm: StateFlow<Double> = todaySteps.map { steps ->
        (steps * 52.1) / 100_000.0 // Métrica de conversão de exemplo
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}