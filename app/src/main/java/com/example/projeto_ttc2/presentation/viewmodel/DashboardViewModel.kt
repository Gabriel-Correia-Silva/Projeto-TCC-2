package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.repository.CaloriesRepository
import com.example.projeto_ttc2.database.repository.HeartRateRepository
import com.example.projeto_ttc2.database.repository.SleepRepository
import com.example.projeto_ttc2.database.repository.StepsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

enum class Period {
    DIA, SEMANA, MES
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val heartRateRepository: HeartRateRepository,
    private val stepsRepository: StepsRepository,
    sleepRepository: SleepRepository,
    caloriesRepository: CaloriesRepository
) : ViewModel() {

    // ... (Estados de Heart Rate e outros)
    val latestHeartRate: StateFlow<Long> = heartRateRepository.getLatestHeartRate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayHeartRateData: StateFlow<List<Long>> = heartRateRepository.getTodayHeartRateData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHeartRateRecords = heartRateRepository.getTodayHeartRateRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _heartRateForDate = MutableStateFlow<List<BatimentoCardiaco>>(emptyList())
    val heartRateForDate: StateFlow<List<BatimentoCardiaco>> = _heartRateForDate.asStateFlow()

    private val _hourlyStepsForDate = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val hourlyStepsForDate: StateFlow<Map<Int, Long>> = _hourlyStepsForDate.asStateFlow()

    // Novos estados para a tela de Passos
    private val _selectedPeriod = MutableStateFlow(Period.SEMANA)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _stepsForPeriod = MutableStateFlow<List<Passos>>(emptyList())
    val stepsForPeriod: StateFlow<List<Passos>> = _stepsForPeriod.asStateFlow()

    private val _totalStepsForPeriod = MutableStateFlow(0L)
    val totalStepsForPeriod: StateFlow<Long> = _totalStepsForPeriod.asStateFlow()

    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
        loadStepsForSelectedPeriod()
    }

    private fun loadStepsForSelectedPeriod() {
        val now = LocalDate.now()
        val (startDate, endDate) = when (_selectedPeriod.value) {
            Period.DIA -> now to now
            Period.SEMANA -> now.with(DayOfWeek.MONDAY) to now.with(DayOfWeek.SUNDAY)
            Period.MES -> now.withDayOfMonth(1) to now.withDayOfMonth(now.lengthOfMonth())
        }

        viewModelScope.launch {
            if (_selectedPeriod.value == Period.DIA) {
                // Lógica para o dia (total e por hora)
                stepsRepository.getStepsForDate(now).collect {
                    _totalStepsForPeriod.value = it?.contagem ?: 0L
                }
                _hourlyStepsForDate.value = stepsRepository.getHourlyStepsForDate(now)
                _stepsForPeriod.value = emptyList() // Limpa os dados de período
            } else {
                // Lógica para semana e mês
                stepsRepository.getStepsForPeriod(startDate, endDate).collect { stepsList ->
                    _stepsForPeriod.value = stepsList
                    _totalStepsForPeriod.value = stepsList.sumOf { it.contagem }
                }
                _hourlyStepsForDate.value = emptyMap() // Limpa os dados por hora
            }
        }
    }

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

    private val _stepsForDate = MutableStateFlow<Passos?>(null)
    val stepsForDate: StateFlow<Passos?> = _stepsForDate.asStateFlow()

    fun loadStepsForDate(date: LocalDate) {
        viewModelScope.launch {
            stepsRepository.getStepsForDate(date).collect {
                _stepsForDate.value = it
            }
        }
    }

    val latestSleepSession: StateFlow<Sono?> = sleepRepository.getLatestSleepSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayActiveCalories: StateFlow<Double> = caloriesRepository.getTodayActiveCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayTotalCalories: StateFlow<Double> = caloriesRepository.getTodayTotalCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayDistanceKm: StateFlow<Double> = todaySteps.map { steps ->
        (steps * 0.762) / 1000 // Métrica de conversão de exemplo
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}