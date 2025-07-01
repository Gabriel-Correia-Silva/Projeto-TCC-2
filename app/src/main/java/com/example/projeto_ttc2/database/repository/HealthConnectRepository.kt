package com.example.projeto_ttc2.database.repository

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import com.example.projeto_ttc2.database.dao.CaloriasDao
import com.example.projeto_ttc2.database.dao.PassosDao
import com.example.projeto_ttc2.database.dao.SonoDao
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Calorias
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.database.entities.Sono
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepository @Inject constructor(
    private val batimentoCardiacoDao: BatimentoCardiacoDao,
    private val passosDao: PassosDao,
    private val sonoDao: SonoDao,
    private val caloriasDao: CaloriasDao
) {
    private var healthConnectClient: HealthConnectClient? = null
    private val TAG = "HealthConnectRepository"

    fun initialize(context: Context) {
        healthConnectClient = HealthConnectClient.getOrCreate(context)
    }

    suspend fun checkPermissions(): Set<String> {
        return healthConnectClient?.permissionController?.getGrantedPermissions() ?: emptySet()
    }

    suspend fun syncHeartRateData() {
        val client = healthConnectClient ?: throw IllegalStateException("HealthConnectClient não inicializado.")
        Log.d(TAG, "Iniciando sincronização de dados de batimentos cardíacos.")

        val fim = Instant.now()
        val inicio = fim.minus(7, ChronoUnit.DAYS)
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(inicio, fim)
        )

        val response = client.readRecords(request)
        Log.d(TAG, "Resposta do Health Connect: ${response.records.size} registros de batimento cardíaco encontrados")

        if (response.records.isEmpty()) {
            Log.w(TAG, "Nenhum registro de batimento cardíaco encontrado no período de 7 dias.")
            return
        }

        Log.i(TAG, "SUCESSO! ${response.records.size} registros de batimento cardíaco encontrados.")

        val entidadesParaInserir = response.records.flatMap { record ->
            record.samples.map { sample ->
                BatimentoCardiaco(
                    timestamp = sample.time,
                    healthConnectId = record.metadata.id + "_" + sample.time.toEpochMilli(),
                    bpm = sample.beatsPerMinute,
                    zoneOffset = record.startZoneOffset
                )
            }
        }

        if (entidadesParaInserir.isNotEmpty()) {
            batimentoCardiacoDao.insertAll(entidadesParaInserir)
            Log.i(TAG, "${entidadesParaInserir.size} amostras de batimento cardíaco salvas no banco de dados.")
        } else {
            Log.w(TAG, "Nenhuma amostra de batimento cardíaco para inserir.")
        }
    }

    suspend fun syncTodaySteps() {
        val client = healthConnectClient ?: throw IllegalStateException("HealthConnectClient não inicializado.")
        val hoje = LocalDate.now()
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val now = Instant.now()

        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
        )
        val response = client.readRecords(request)
        val totalPassos = response.records.sumOf { it.count }

        Log.d(TAG, "Total de passos de hoje do Health Connect: $totalPassos")
        passosDao.upsert(Passos(data = hoje, contagem = totalPassos))
        Log.d(TAG, "Dados de passos salvos no banco de dados para $hoje.")
    }

    suspend fun syncSleepData() {
        val client = healthConnectClient ?: return
        val yesterday = ZonedDateTime.now().minus(1, ChronoUnit.DAYS).toInstant()

        try {
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.after(yesterday)
            )
            val response = client.readRecords(request)
            val sonoEntities = response.records.map { record ->
                val awake = record.stages
                    .filter { it.stage == SleepSessionRecord.STAGE_TYPE_AWAKE }
                    .sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                val rem = record.stages
                    .filter { it.stage == SleepSessionRecord.STAGE_TYPE_REM }
                    .sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                val deep = record.stages
                    .filter { it.stage == SleepSessionRecord.STAGE_TYPE_DEEP }
                    .sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                val light = record.stages
                    .filter { it.stage == SleepSessionRecord.STAGE_TYPE_LIGHT }
                    .sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }

                Sono(
                    healthConnectId = record.metadata.id,
                    startTime = record.startTime,
                    endTime = record.endTime,
                    durationMinutes = java.time.Duration.between(record.startTime, record.endTime).toMinutes(),
                    awakeDurationMinutes = awake,
                    remSleepDurationMinutes = rem,
                    deepSleepDurationMinutes = deep,
                    lightSleepDurationMinutes = light
                )
            }
            if (sonoEntities.isNotEmpty()) {
                sonoDao.insertAll(sonoEntities)
                Log.d(TAG, "${sonoEntities.size} sessões de sono inseridas/atualizadas.")
            } else {
                Log.d(TAG, "Nenhuma nova sessão de sono encontrada.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de sono", e)
        }
    }

    suspend fun syncCaloriesData() {
        val client = healthConnectClient ?: return
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val now = Instant.now()
        val timeRangeFilter = TimeRangeFilter.between(startOfDay, now)

        val activeCaloriesRequest = ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, timeRangeFilter)
        val totalCaloriesRequest = ReadRecordsRequest(TotalCaloriesBurnedRecord::class, timeRangeFilter)

        try {
            val activeResponse = client.readRecords(activeCaloriesRequest)
            val activeEntities = activeResponse.records.map { record ->
                Calorias(
                    healthConnectId = record.metadata.id,
                    startTime = record.startTime,
                    endTime = record.endTime,
                    kilocalorias = record.energy.inKilocalories,
                    tipo = "ATIVA"
                )
            }
            if (activeEntities.isNotEmpty()) {
                caloriasDao.insertAll(activeEntities)
                Log.d(TAG, "${activeEntities.size} registros de calorias ativas inseridos.")
            }

            val totalResponse = client.readRecords(totalCaloriesRequest)
            val totalEntities = totalResponse.records.map { record ->
                Calorias(
                    healthConnectId = record.metadata.id,
                    startTime = record.startTime,
                    endTime = record.endTime,
                    kilocalorias = record.energy.inKilocalories,
                    tipo = "TOTAL"
                )
            }
            if (totalEntities.isNotEmpty()) {
                caloriasDao.insertAll(totalEntities)
                Log.d(TAG, "${totalEntities.size} registros de calorias totais inseridos.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar dados de calorias", e)
            throw e // Re-lança a exceção para que o ViewModel possa tratá-la
        }
    }


    fun getLatestHeartRate(): Flow<Long> {
        return batimentoCardiacoDao.getUltimoBatimento().map { it?.bpm ?: 0L }
    }

    fun getTodayStepsFlow(): Flow<Long> {
        return passosDao.getPassosPorData(LocalDate.now()).map { it?.contagem ?: 0L }
    }

    fun getLatestSleepSession(): Flow<Sono?> {
        return sonoDao.getUltimaSessaoSono()
    }

    fun getTodayActiveCalories(): Flow<Double> {
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return caloriasDao.getSomaCaloriasPorTipoDesde("ATIVA", startOfDay).map { it ?: 0.0 }
    }

    fun getTodayTotalCalories(): Flow<Double> {
        val startOfDay = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return caloriasDao.getSomaCaloriasPorTipoDesde("TOTAL", startOfDay).map { it ?: 0.0 }
    }

    companion object {
        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
        )
    }
}