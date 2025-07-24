package com.example.projeto_ttc2.background

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleConnectionManager @Inject constructor() {
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

    fun setServiceRunning(isRunning: Boolean) {
        _isServiceRunning.value = isRunning
    }
}