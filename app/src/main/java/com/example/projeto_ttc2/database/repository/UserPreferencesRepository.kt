package com.example.projeto_ttc2.database.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "user_goals_prefs"
private const val KEY_HEART_RATE_THRESHOLD = "heart_rate_threshold"
private const val KEY_STEP_GOAL = "step_goal"
private const val KEY_SLEEP_MONITORING_ENABLED = "sleep_monitoring_enabled"

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _heartRateThreshold = MutableStateFlow(prefs.getLong(KEY_HEART_RATE_THRESHOLD, 120L))
    val heartRateThreshold: StateFlow<Long> = _heartRateThreshold

    private val _stepGoal = MutableStateFlow(prefs.getLong(KEY_STEP_GOAL, 10000L))
    val stepGoal: StateFlow<Long> = _stepGoal


    private val _sleepMonitoringEnabled = MutableStateFlow(prefs.getBoolean(KEY_SLEEP_MONITORING_ENABLED, false))
    val sleepMonitoringEnabled: StateFlow<Boolean> = _sleepMonitoringEnabled


    fun setHeartRateThreshold(threshold: Long) {
        prefs.edit().putLong(KEY_HEART_RATE_THRESHOLD, threshold).apply()
        _heartRateThreshold.value = threshold
    }

    fun setStepGoal(goal: Long) {
        prefs.edit().putLong(KEY_STEP_GOAL, goal).apply()
        _stepGoal.value = goal
    }


    fun setSleepMonitoringEnabled(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_SLEEP_MONITORING_ENABLED, isEnabled).apply()
        _sleepMonitoringEnabled.value = isEnabled
    }

}