package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.repository.CaloriesRepository
import com.example.projeto_ttc2.database.repository.HeartRateRepository
import com.example.projeto_ttc2.database.repository.SleepRepository
import com.example.projeto_ttc2.database.repository.StepsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val heartRateRepository: HeartRateRepository,
    stepsRepository: StepsRepository,
    sleepRepository: SleepRepository,
    caloriesRepository: CaloriesRepository
) : ViewModel() {

    val latestHeartRate: StateFlow<Long> = heartRateRepository.getLatestHeartRate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayHeartRateData: StateFlow<List<Long>> = heartRateRepository.getTodayHeartRateData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHeartRateRecords = heartRateRepository.getTodayHeartRateRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _heartRateForDate = MutableStateFlow<List<BatimentoCardiaco>>(emptyList())
    val heartRateForDate: StateFlow<List<BatimentoCardiaco>> = _heartRateForDate.asStateFlow()

    fun loadHeartRateForDate(date: LocalDate) {
        viewModelScope.launch {
            heartRateRepository.getHeartRateRecordsForDate(date)
                .collect { data ->
                    _heartRateForDate.value = data
                }
        }
    }

    val todaySteps: StateFlow<Long> = stepsRepository.getTodayStepsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val latestSleepSession: StateFlow<Sono?> = sleepRepository.getLatestSleepSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayActiveCalories: StateFlow<Double> = caloriesRepository.getTodayActiveCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayTotalCalories: StateFlow<Double> = caloriesRepository.getTodayTotalCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayDistanceKm: StateFlow<Double> = todaySteps.map { steps ->
        (steps * 52.1) / 100_000.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}