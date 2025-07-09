package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.entities.SonoWithStages
import com.example.projeto_ttc2.database.repository.*
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
    private val sleepRepository: SleepRepository,
    private val caloriesRepository: CaloriesRepository,
    private val oxygenSaturationRepository: OxygenSaturationRepository,
    private val feedbackRepository: FeedbackRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {


    val heartRateThreshold: StateFlow<Long> = userPreferencesRepository.heartRateThreshold
    val stepGoal: StateFlow<Long> = userPreferencesRepository.stepGoal

    val oxygenationHistory: StateFlow<List<Double>> = oxygenSaturationRepository.getLatestSevenOxygenationReadings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showHeartRateAlert = MutableStateFlow(false)
    val showHeartRateAlert: StateFlow<Boolean> = _showHeartRateAlert.asStateFlow()

    val latestHeartRate: StateFlow<Long> = heartRateRepository.getLatestHeartRate()
        .onEach { rate ->
            if (rate > heartRateThreshold.value) {
                _showHeartRateAlert.value = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setHeartRateThreshold(threshold: Long) {
        userPreferencesRepository.setHeartRateThreshold(threshold)
    }

    fun setStepGoal(goal: Long) {
        userPreferencesRepository.setStepGoal(goal)
    }

    val todayHeartRateData: StateFlow<List<Long>> = heartRateRepository.getTodayHeartRateData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHeartRateRecords = heartRateRepository.getTodayHeartRateRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _heartRateForDate = MutableStateFlow<List<BatimentoCardiaco>>(emptyList())
    val heartRateForDate: StateFlow<List<BatimentoCardiaco>> = _heartRateForDate.asStateFlow()

    private val _hourlyStepsForDate = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val hourlyStepsForDate: StateFlow<Map<Int, Long>> = _hourlyStepsForDate.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(Period.SEMANA)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _stepsForPeriod = MutableStateFlow<List<Passos>>(emptyList())
    val stepsForPeriod: StateFlow<List<Passos>> = _stepsForPeriod.asStateFlow()

    private val _totalStepsForPeriod = MutableStateFlow(0L)
    val totalStepsForPeriod: StateFlow<Long> = _totalStepsForPeriod.asStateFlow()

    val latestOxygenSaturation: StateFlow<Double> = oxygenSaturationRepository.getLatestOxygenSaturation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val unreadFeedbackCount: StateFlow<Int> =
        authRepository.getCurrentUserFlow().flatMapLatest { user ->
            feedbackRepository.getUnreadFeedbackCount(user?.uid ?: "")
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun dismissHeartRateAlert() {
        _showHeartRateAlert.value = false
    }

    fun triggerEmergencyActions(context: Context, emergencyContact: String?) {
        // TODO: Implementar notificação para o médico (ex: via Push com Firebase)
        // notifyDoctorAboutHeartRatePeak()

        emergencyContact?.let { phone ->
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phone")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(callIntent)
            } catch (e: SecurityException) {
                // Lidar com o caso de a permissão CALL_PHONE não ter sido concedida
            }
        }
        dismissHeartRateAlert()
    }

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
                stepsRepository.getStepsForDate(now).collect {
                    _totalStepsForPeriod.value = it?.contagem ?: 0L
                }
                _hourlyStepsForDate.value = stepsRepository.getHourlyStepsForDate(now)
                _stepsForPeriod.value = emptyList()
            } else {
                stepsRepository.getStepsForPeriod(startDate, endDate).collect { stepsList ->
                    _stepsForPeriod.value = stepsList
                    _totalStepsForPeriod.value = stepsList.sumOf { it.contagem }
                }
                _hourlyStepsForDate.value = emptyMap()
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

    val latestSleepSession: StateFlow<Sono?> = sleepRepository.getLatestSleepSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestSleepSessionWithStages: StateFlow<SonoWithStages?> = sleepRepository.getLatestSleepSessionWithStages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val todayActiveCalories: StateFlow<Double> = caloriesRepository.getTodayActiveCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayTotalCalories: StateFlow<Double> = caloriesRepository.getTodayTotalCalories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayDistanceKm: StateFlow<Double> = todaySteps.map { steps ->
        (steps * 0.762) / 1000
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}