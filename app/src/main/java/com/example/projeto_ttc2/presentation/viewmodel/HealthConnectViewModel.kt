package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.projeto_ttc2.database.local.DashboardData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.collections.containsAll
import kotlin.text.compareTo

class HealthConnectViewModel : ViewModel() {

    // Cliente para interagir com o Health Connect
    private lateinit var healthConnectClient: HealthConnectClient

    private var isRequestingPermission = false

    // Estado da UI para refletir o que está acontecendo (carregando, sucesso, erro)
    val uiState = mutableStateOf<UiState>(UiState.Uninitialized)

    // Estado dos dados do dashboard
    private val _dashboardData = kotlinx.coroutines.flow.MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData

    // Canal para enviar eventos únicos para a UI (como pedir permissão)
    private val _permissionRequestChannel = Channel<Set<String>>()
    val permissionRequestChannel = _permissionRequestChannel.receiveAsFlow()

    // Permissões que o app precisa para popular o dashboard
    private val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class)
        )

    fun initialLoad(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
            if (!isRequestingPermission) {
                checkPermissionsAndFetchData()
            }
        } else {
            uiState.value = UiState.Error("Health Connect não é suportado nesta versão do Android")
        }
    }

    private fun checkPermissionsAndFetchData() {
        viewModelScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(PERMISSIONS)) {
                isRequestingPermission = false
                fetchDashboardData()
            } else {
                if (!isRequestingPermission) {
                    isRequestingPermission = true
                    _permissionRequestChannel.send(PERMISSIONS)
                }
            }
        }
    }


    fun onPermissionsResult(granted: Set<String>) {
        isRequestingPermission = false
        if (granted.containsAll(PERMISSIONS)) {
            fetchDashboardData()
        } else {
            uiState.value = UiState.Error("Permissões necessárias não foram concedidas.")
        }
    }

    private fun fetchDashboardData() {
        viewModelScope.launch {
            uiState.value = UiState.Loading
            try {
                val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                val now = Instant.now()

                // Busca cada dado em paralelo (mais eficiente)
                val steps = readSteps(startOfDay.toInstant(), now)
                val distance = readDistance(startOfDay.toInstant(), now)
                val calories = readCalories(startOfDay.toInstant(), now)
                val heartRate = readLatestHeartRate()
                val sleep = readSleep(startOfDay.minusDays(1).toInstant(), now)

                // Atualiza o StateFlow com os dados lidos
                _dashboardData.value = DashboardData(
                    steps = steps,
                    distanceKm = distance / 1000.0, // Converte metros para km
                    caloriesKcal = calories,
                    activeCaloriesKcal = 0.0, // Health Connect não separa facilmente ativos de total
                    heartRate = heartRate,
                    sleepDurationMinutes = sleep
                )
                uiState.value = UiState.Success
            } catch (e: Exception) {
                uiState.value = UiState.Error(e.message ?: "Falha ao ler dados do Health Connect")
            }
        }
    }

    // --- Funções de Leitura de Dados ---

    private suspend fun readSteps(start: Instant, end: Instant): Long {
        val request = ReadRecordsRequest(StepsRecord::class, TimeRangeFilter.between(start, end))
        val response = healthConnectClient.readRecords(request)
        return response.records.sumOf { it.count }
    }

    private suspend fun readDistance(start: Instant, end: Instant): Double {
        val request = ReadRecordsRequest(DistanceRecord::class, TimeRangeFilter.between(start, end))
        val response = healthConnectClient.readRecords(request)
        return response.records.sumOf { it.distance.inMeters }
    }

    private suspend fun readCalories(start: Instant, end: Instant): Double {
        val request = ReadRecordsRequest(TotalCaloriesBurnedRecord::class, TimeRangeFilter.between(start, end))
        val response = healthConnectClient.readRecords(request)
        return response.records.sumOf { it.energy.inKilocalories }
    }

    private suspend fun readLatestHeartRate(): Long {
        val request = ReadRecordsRequest(HeartRateRecord::class, TimeRangeFilter.before(Instant.now()))
        val response = healthConnectClient.readRecords(request)
        // Pega a amostra mais recente, se houver
        return response.records.flatMap { it.samples }.maxByOrNull { it.time }?.beatsPerMinute ?: 0L
    }

    private suspend fun readSleep(start: Instant, end: Instant): Long {
        val request = ReadRecordsRequest(SleepSessionRecord::class, TimeRangeFilter.between(start, end))
        val response = healthConnectClient.readRecords(request)
        val totalDuration = response.records.sumOf { it.endTime.toEpochMilli() - it.startTime.toEpochMilli() }
        return totalDuration / (1000 * 60) // Converte milissegundos para minutos
    }
}

// Sealed class para representar os estados da UI de forma clara
sealed class UiState {
    object Uninitialized : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}