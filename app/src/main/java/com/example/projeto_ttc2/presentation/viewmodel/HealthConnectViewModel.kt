// Local: com/example/projeto_ttc2/presentation/viewmodel/HealthConnectViewModel.kt
package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.repository.HealthConnectManager
import com.example.projeto_ttc2.database.repository.SyncRepository
import com.example.projeto_ttc2.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val TAG = "HealthConnectViewModel"
    private var isRequestingPermission = false

    val permissions: Set<String> = HealthConnectManager.REQUIRED_PERMISSIONS

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
                syncData(showIndicator = false) // Chama sem indicador na carga inicial
            } else {
                uiState.value = UiState.PermissionRequired
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            isRequestingPermission = false
            if (granted.containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)) {
                syncData(showIndicator = true) // Mostra indicador após conceder permissão
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

    // Adicionando métodos que estão sendo referenciados no Navigation.kt
    fun permissionsGranted(context: Context, permissions: Set<String>? = null): Boolean {
        return try {
            if (permissions != null) {
                permissions.containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)
            } else {
                // Verifica se já tem as permissões concedidas
                val grantedPermissions = healthConnectManager.getGrantedPermissions()
                grantedPermissions.containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)
            }
        } catch (e: Exception) {
            false // Se há erro, assume que não tem permissões
        }
    }

    fun requestPermissions(launcher: androidx.activity.result.ActivityResultLauncher<Set<String>>) {
        if (!isRequestingPermission) {
            isRequestingPermission = true
            launcher.launch(HealthConnectManager.REQUIRED_PERMISSIONS)
        }
    }
}