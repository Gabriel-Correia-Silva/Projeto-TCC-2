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
private const val KEY_HEART_RATE_INTERVAL = "heart_rate_interval"
private const val KEY_SPO2_INTERVAL = "spo2_interval"
private const val KEY_OVERRIDE_HEALTH_CONNECT = "override_health_connect"
private const val KEY_MONITORING_PAUSED_UNTIL = "monitoring_paused_until"

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

    private val _heartRateInterval = MutableStateFlow(prefs.getInt(KEY_HEART_RATE_INTERVAL, 10))
    val heartRateInterval: StateFlow<Int> = _heartRateInterval

    private val _spo2Interval = MutableStateFlow(prefs.getInt(KEY_SPO2_INTERVAL, 600))
    val spo2Interval: StateFlow<Int> = _spo2Interval

    private val _overrideHealthConnect = MutableStateFlow(prefs.getBoolean(KEY_OVERRIDE_HEALTH_CONNECT, false))
    val overrideHealthConnect: StateFlow<Boolean> = _overrideHealthConnect

    private val _monitoringPausedUntil = MutableStateFlow(prefs.getLong(KEY_MONITORING_PAUSED_UNTIL, 0L))
    val monitoringPausedUntil: StateFlow<Long> = _monitoringPausedUntil

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

    fun setOverrideHealthConnect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERRIDE_HEALTH_CONNECT, enabled).apply()
        _overrideHealthConnect.value = enabled
    }

    fun setHeartRateInterval(intervalInSeconds: Int) {
        prefs.edit().putInt(KEY_HEART_RATE_INTERVAL, intervalInSeconds).apply()
        _heartRateInterval.value = intervalInSeconds
    }

    fun setSpo2Interval(intervalInSeconds: Int) {
        prefs.edit().putInt(KEY_SPO2_INTERVAL, intervalInSeconds).apply()
        _spo2Interval.value = intervalInSeconds
    }

    fun isAnyRawDataEnabled(): Boolean {
        return accelerometerEnabled.value
    }
    fun setMonitoringPausedUntil(timestamp: Long) {
        prefs.edit().putLong(KEY_MONITORING_PAUSED_UNTIL, timestamp).apply()
        _monitoringPausedUntil.value = timestamp
    }
}