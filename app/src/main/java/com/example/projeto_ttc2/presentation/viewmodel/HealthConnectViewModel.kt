package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.repository.*
import com.example.projeto_ttc2.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val heartRateRepository: HeartRateRepository,
    private val stepsRepository: StepsRepository,
    private val sleepRepository: SleepRepository,
    private val caloriesRepository: CaloriesRepository
) : ViewModel() {

    private val TAG = "HealthConnectViewModel"
    private var isRequestingPermission = false

    val uiState = mutableStateOf<UiState>(UiState.Uninitialized)
    private val _permissionRequestChannel = Channel<Set<String>>()
    val permissionRequestChannel = _permissionRequestChannel.receiveAsFlow()

    fun initialLoad(context: Context) {
        if (HealthConnectClient.getSdkStatus(context.applicationContext) != HealthConnectClient.SDK_AVAILABLE) {
            uiState.value = UiState.Error("Health Connect não está disponível ou precisa ser atualizado")
            return
        }
        healthConnectManager.initialize(context.applicationContext)
        checkPermissionsAndFetchData()
    }

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectManager.getGrantedPermissions().containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)
    }

    private fun checkPermissionsAndFetchData() {
        viewModelScope.launch {
            if (hasAllPermissions()) {
                isRequestingPermission = false
                syncData()
            } else {
                uiState.value = UiState.PermissionRequired
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            isRequestingPermission = false
            if (granted.containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)) {
                syncData()
            } else {
                uiState.value = UiState.Error("As permissões de saúde são necessárias para o funcionamento do app.")
            }
        }
    }

    fun requestPermissions() {
        viewModelScope.launch {
            if (!isRequestingPermission) {
                isRequestingPermission = true
                _permissionRequestChannel.send(HealthConnectManager.REQUIRED_PERMISSIONS)
            }
        }
    }

    fun syncData(): Job {
        return viewModelScope.launch {
            uiState.value = UiState.Loading
            try {
                val syncTasks = listOf(
                    async { heartRateRepository.syncData() },
                    async { stepsRepository.syncData() },
                    async { sleepRepository.syncData() },
                    async { caloriesRepository.syncData() }
                )
                syncTasks.awaitAll()
                uiState.value = UiState.Success
            } catch (e: Exception) {
                Log.e(TAG, "FALHA ao sincronizar dados.", e)
                uiState.value = UiState.Error(e.message ?: "Erro desconhecido ao sincronizar dados")
            }
        }
    }
}