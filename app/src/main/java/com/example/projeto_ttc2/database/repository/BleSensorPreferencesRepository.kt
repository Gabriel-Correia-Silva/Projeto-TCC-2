package com.example.projeto_ttc2.database.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "ble_sensor_prefs"
private const val KEY_ACCELEROMETER_ENABLED = "accelerometer_enabled"
private const val KEY_HEART_RATE_ENABLED = "heart_rate_enabled"
private const val KEY_SPO2_ENABLED = "spo2_enabled"
private const val KEY_STRESS_ENABLED = "stress_enabled"
private const val KEY_STEPS_GENERAL_ENABLED = "steps_general_enabled"

@Singleton
class BleSensorPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {


    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _accelerometerEnabled = MutableStateFlow(prefs.getBoolean(KEY_ACCELEROMETER_ENABLED, false))
    val accelerometerEnabled: StateFlow<Boolean> = _accelerometerEnabled

    private val _heartRateEnabled = MutableStateFlow(prefs.getBoolean(KEY_HEART_RATE_ENABLED, false))
    val heartRateEnabled: StateFlow<Boolean> = _heartRateEnabled

    private val _spo2Enabled = MutableStateFlow(prefs.getBoolean(KEY_SPO2_ENABLED, false))
    val spo2Enabled: StateFlow<Boolean> = _spo2Enabled

    private val _stressEnabled = MutableStateFlow(prefs.getBoolean(KEY_STRESS_ENABLED, false))
    val stressEnabled: StateFlow<Boolean> = _stressEnabled

    private val _stepsGeneralEnabled = MutableStateFlow(prefs.getBoolean(KEY_STEPS_GENERAL_ENABLED, false))
    val stepsGeneralEnabled: StateFlow<Boolean> = _stepsGeneralEnabled

    fun setAccelerometerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ACCELEROMETER_ENABLED, enabled).apply()
        _accelerometerEnabled.value = enabled
    }

    fun setHeartRateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HEART_RATE_ENABLED, enabled).apply()
        _heartRateEnabled.value = enabled
    }

    fun setSpO2Enabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPO2_ENABLED, enabled).apply()
        _spo2Enabled.value = enabled
    }

    fun setStressEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRESS_ENABLED, enabled).apply()
        _stressEnabled.value = enabled
    }

    fun setStepsGeneralEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STEPS_GENERAL_ENABLED, enabled).apply()
        _stepsGeneralEnabled.value = enabled
    }


    fun isAnyRawDataEnabled(): Boolean {
        return accelerometerEnabled.value ||
                false
    }
}