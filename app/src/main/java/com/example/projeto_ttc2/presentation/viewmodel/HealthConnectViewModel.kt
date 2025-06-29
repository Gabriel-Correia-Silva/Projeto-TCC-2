package com.example.projeto_ttc2.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import com.example.projeto_ttc2.database.repository.HealthConnectRepository
import com.example.projeto_ttc2.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository,
    private val batimentoCardiacoDao: BatimentoCardiacoDao // Mantido para debug
) : ViewModel() {

    private val TAG = "HealthConnectViewModel"
    private var isRequestingPermission = false
    @SuppressLint("StaticFieldLeak")
    private var currentContext: Context? = null

    val uiState = mutableStateOf<UiState>(UiState.Uninitialized)
    private val _permissionRequestChannel = Channel<Set<String>>()
    val permissionRequestChannel = _permissionRequestChannel.receiveAsFlow()

    val latestHeartRate = healthConnectRepository.getLatestHeartRate()

    val todaySteps: StateFlow<Long> = healthConnectRepository.getTodayStepsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayDistanceKm: StateFlow<Double> = todaySteps.map { steps ->
        (steps * 52.1) / 100_000.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    private fun checkHealthConnectAvailability(context: Context): Boolean {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> true
            else -> false
        }
    }

    fun initializeRepository(context: Context) {
        healthConnectRepository.initialize(context)
    }

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectRepository.checkPermissions().containsAll(HealthConnectRepository.REQUIRED_PERMISSIONS)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun initialLoad(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            currentContext = context
            if (!checkHealthConnectAvailability(context)) {
                uiState.value = UiState.Error("Health Connect não está disponível ou precisa ser atualizado")
                return
            }

            healthConnectRepository.initialize(context) // Garante inicialização
            checkPermissionsAndFetchData()

        } else {
            uiState.value = UiState.Error("Health Connect não é suportado nesta versão do Android")
        }
    }

    private fun checkPermissionsAndFetchData() {
        viewModelScope.launch {
            if (hasAllPermissions()) {
                isRequestingPermission = false
                syncData()
            } else {
                if (!isRequestingPermission) {
                    isRequestingPermission = true
                    _permissionRequestChannel.send(HealthConnectRepository.REQUIRED_PERMISSIONS)
                }
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        isRequestingPermission = false
        if (granted.containsAll(HealthConnectRepository.REQUIRED_PERMISSIONS)) {
            syncData()
        } else {
            uiState.value = UiState.Error("Permissão para ler dados de saúde não foi concedida.")
        }
    }

    fun syncData(): Job {
        return viewModelScope.launch {
            uiState.value = UiState.Loading
            try {
                healthConnectRepository.syncHeartRateData()
                healthConnectRepository.syncTodaySteps()
                uiState.value = UiState.Success
            } catch (e: Exception) {
                Log.e(TAG, "FALHA ao sincronizar dados.", e)
                uiState.value = UiState.Error(e.message ?: "Erro desconhecido ao sincronizar dados")
            }
        }
    }

    fun debugDatabaseInfo() {
        viewModelScope.launch {
            try {
                val total = batimentoCardiacoDao.getTotalRegistros()
                val ultimos10 = batimentoCardiacoDao.getUltimos10Batimentos()

                Log.d(TAG, "=== DEBUG DATABASE INFO ===")
                Log.d(TAG, "Total de registros: $total")
                ultimos10.forEach { batimento ->
                    Log.d(TAG, "ID: ${batimento.id}, BPM: ${batimento.bpm}, Timestamp: ${batimento.timestamp}")
                }
                Log.d(TAG, "===========================")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar dados do banco", e)
            }
        }
    }
}