package com.example.projeto_ttc2.presentation.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.repository.HeartRateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val batimentoCardiacoDao: BatimentoCardiacoDao,
    private val heartRateRepository: HeartRateRepository
) : ViewModel() {

    private val TAG = "HealthConnectViewModel"
    private lateinit var healthConnectClient: HealthConnectClient
    private var isRequestingPermission = false
    private var currentContext: Context? = null

    val uiState = mutableStateOf<UiState>(UiState.Uninitialized)
    private val _permissionRequestChannel = Channel<Set<String>>()
    val permissionRequestChannel = _permissionRequestChannel.receiveAsFlow()

    private val _latestHeartRate = MutableStateFlow(0L)
    val latestHeartRate: StateFlow<Long> = _latestHeartRate

    private val PERMISSIONS =
        setOf(HealthPermission.getReadPermission(HeartRateRecord::class))

    init {
        fetchLatestHeartRateFromDb()
    }

    private fun checkHealthConnectAvailability(context: Context): Boolean {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> {
                Log.d(TAG, "Health Connect está disponível")
                true
            }
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Log.e(TAG, "Health Connect não está disponível neste dispositivo")
                false
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                Log.e(TAG, "Health Connect precisa ser atualizado")
                false
            }
            else -> {
                Log.e(TAG, "Status desconhecido do Health Connect")
                false
            }
        }
    }

    fun initialLoad(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            currentContext = context
            if (!checkHealthConnectAvailability(context)) {
                uiState.value = UiState.Error("Health Connect não está disponível ou precisa ser atualizado")
                return
            }

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
                currentContext?.let { readAndSaveHeartRateData(it) }
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
            currentContext?.let { readAndSaveHeartRateData(it) }
        } else {
            uiState.value = UiState.Error("Permissão para ler batimentos cardíacos não foi concedida.")
        }
    }

    private fun readAndSaveHeartRateData(context: Context) {
        viewModelScope.launch {
            uiState.value = UiState.Loading
            Log.d(TAG, "Iniciando leitura e salvamento de dados de batimentos cardíacos.")
            try {
                val availability = HealthConnectClient.getSdkStatus(context)
                if (availability != HealthConnectClient.SDK_AVAILABLE) {
                    val errorMessage = when (availability) {
                        HealthConnectClient.SDK_UNAVAILABLE -> "Health Connect não está disponível neste dispositivo"
                        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "Health Connect precisa ser atualizado"
                        else -> "Status desconhecido do Health Connect"
                    }
                    throw Exception(errorMessage)
                }

                val fim = Instant.now()
                val inicio = fim.minus(7, ChronoUnit.DAYS)
                val request = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(inicio, fim)
                )

                val response = healthConnectClient.readRecords(request)
                Log.d(TAG, "Resposta do Health Connect: ${response.records.size} registros encontrados")

                if (response.records.isEmpty()) {
                    Log.w(TAG, "Nenhum registro de batimento cardíaco encontrado no período de 7 dias.")
                    uiState.value = UiState.Error("Nenhum dado de batimento cardíaco encontrado nos últimos 7 dias")
                } else {
                    Log.i(TAG, "SUCESSO! ${response.records.size} registros de batimento cardíaco encontrados.")

                    val entidadesParaInserir = response.records.flatMap { record ->
                        record.samples.map { sample ->
                            // CORREÇÃO: Log para debug do timestamp original
                            Log.d(TAG, "Timestamp original: ${sample.time}")
                            Log.d(TAG, "Timestamp em millis: ${sample.time.toEpochMilli()}")
                            Log.d(TAG, "Data formatada: ${sample.time}")

                            BatimentoCardiaco(
                                healthConnectId = record.metadata.id,
                                bpm = sample.beatsPerMinute,
                                timestamp = sample.time, // Não precisa converter, já é Instant
                                zoneOffset = record.endZoneOffset
                            )
                        }
                    }

                    if (entidadesParaInserir.isNotEmpty()) {
                        entidadesParaInserir.forEach { batimento ->
                            Log.d(TAG, "Inserindo: BPM=${batimento.bpm}, Timestamp=${batimento.timestamp}")
                        }

                        batimentoCardiacoDao.insertAll(entidadesParaInserir)
                        Log.i(TAG, "${entidadesParaInserir.size} amostras foram salvas no banco de dados.")
                        fetchLatestHeartRateFromDb()
                        uiState.value = UiState.Success
                    } else {
                        uiState.value = UiState.Error("Nenhuma amostra válida encontrada nos registros")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "FALHA ao ler ou salvar dados de batimento cardíaco.", e)
                uiState.value = UiState.Error(e.message ?: "Erro desconhecido ao acessar Health Connect")
            }
        }
    }

    private fun fetchLatestHeartRateFromDb() {
        viewModelScope.launch {
            Log.d(TAG, "Buscando o último batimento cardíaco no banco de dados local...")
            val ultimoBpmDoBanco = heartRateRepository.getLatestHeartRateBpm()
            Log.i(TAG, ">>> ÚLTIMO BATIMENTO ENCONTRADO NO BANCO: $ultimoBpmDoBanco bpm <<<")
            _latestHeartRate.value = ultimoBpmDoBanco
        }
    }

    fun debugDatabaseInfo() {
        viewModelScope.launch {
            try {
                val total = batimentoCardiacoDao.getTotalRegistros()
                val ultimos10 = batimentoCardiacoDao.getUltimos10Batimentos()

                Log.d(TAG, "=== DEBUG DATABASE INFO ===")
                Log.d(TAG, "Total de registros: $total")
                Log.d(TAG, "Registros encontrados:")
                ultimos10.forEach { batimento ->
                    Log.d(TAG, "ID: ${batimento.id}, BPM: ${batimento.bpm}")
                    Log.d(TAG, "Timestamp: ${batimento.timestamp}")
                    Log.d(TAG, "Timestamp em millis: ${batimento.timestamp.toEpochMilli()}")
                    Log.d(TAG, "Data legível: ${java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(batimento.timestamp.atZone(java.time.ZoneId.systemDefault()))}")
                    Log.d(TAG, "---")
                }
                Log.d(TAG, "===========================")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar dados do banco", e)
            }
        }
    }
}

sealed class UiState {
    object Uninitialized : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}

