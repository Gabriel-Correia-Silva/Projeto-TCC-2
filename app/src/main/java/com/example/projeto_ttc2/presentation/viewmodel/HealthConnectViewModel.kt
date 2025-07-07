package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.repository.HealthConnectManager
import com.example.projeto_ttc2.database.repository.SyncRepository
import com.example.projeto_ttc2.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val TAG = "HealthConnectViewModel"

    var isReady by mutableStateOf(false)
        private set

    val permissions: Set<String> = HealthConnectManager.REQUIRED_PERMISSIONS
    val uiState = mutableStateOf<UiState>(UiState.Uninitialized)

    fun initialLoad(context: Context) {
        if (HealthConnectClient.getSdkStatus(context.applicationContext) != HealthConnectClient.SDK_AVAILABLE) {
            uiState.value = UiState.Error("Health Connect não está disponível ou precisa ser atualizado")
            return
        }
        healthConnectManager.initialize(context.applicationContext)
        isReady = true
    }

    suspend fun hasAllPermissions(): Boolean {
        if (!isReady) {
            kotlinx.coroutines.delay(500)
        }
        return healthConnectManager.getGrantedPermissions().containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)
    }

    fun onPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            if (granted.containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)) {
                syncData(showIndicator = true)
            } else {
                uiState.value = UiState.Error("As permissões de saúde são necessárias para o funcionamento do app.")
            }
        }
    }

    fun requestPermissions(launcher: ActivityResultLauncher<Set<String>>) {
        launcher.launch(HealthConnectManager.REQUIRED_PERMISSIONS)
    }

    fun syncData(showIndicator: Boolean = false): Job {
        return viewModelScope.launch {
            if (showIndicator) {
                uiState.value = UiState.Loading
            }
            try {
                syncRepository.syncAllData()
                uiState.value = UiState.Success
            } catch (e: Exception) {
                Log.e(TAG, "FALHA ao sincronizar dados.", e)
                uiState.value = UiState.Error(e.message ?: "Erro desconhecido ao sincronizar dados")
            }
        }
    }
}