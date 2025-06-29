package com.example.projeto_ttc2.database.repository

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import com.example.projeto_ttc2.database.dao.PassosDao
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Passos
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
    private val passosDao: PassosDao

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
                    healthConnectId = record.metadata.id,
                    bpm = sample.beatsPerMinute,
                    timestamp = sample.time,
                    zoneOffset = record.endZoneOffset
                )
            }
        }

        if (entidadesParaInserir.isNotEmpty()) {
            batimentoCardiacoDao.insertAll(entidadesParaInserir)
            Log.i(TAG, "${entidadesParaInserir.size} amostras foram salvas no banco de dados.")
        } else {
            Log.w(TAG, "Nenhuma amostra válida encontrada nos registros para salvar.")
        }
    }suspend fun syncTodaySteps() {
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

    fun getLatestHeartRate(): Flow<Long> {
        return batimentoCardiacoDao.getUltimoBatimento().map { it?.bpm ?: 0L }
    }

    fun getTodayStepsFlow(): Flow<Long> {
        return passosDao.getPassosPorData(LocalDate.now()).map { it?.contagem ?: 0L }
    }

    companion object {
        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }
}