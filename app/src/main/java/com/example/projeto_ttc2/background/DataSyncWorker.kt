package com.example.projeto_ttc2.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.database.repository.HealthConnectManager
import com.example.projeto_ttc2.database.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams) {

    private val notificationId = 1
    private val channelId = "DataSyncChannel"

    override suspend fun doWork(): Result {
        Log.d("DataSyncWorker", "Iniciando sincronização de dados em segundo plano.")

        val healthConnectManager = HealthConnectManager(appContext)
        healthConnectManager.initialize(appContext)
        val hasPermissions = healthConnectManager.getGrantedPermissions().containsAll(HealthConnectManager.REQUIRED_PERMISSIONS)

        if (!hasPermissions) {
            Log.w("DataSyncWorker", "Permissões não concedidas. Adiando a sincronização.")
            return Result.failure()
        }

        try {
            setForeground(createForegroundInfo())
            syncRepository.syncAllData()
            Log.d("DataSyncWorker", "Sincronização de dados em segundo plano concluída com sucesso.")
            return Result.success()
        } catch (e: Exception) {
            Log.e("DataSyncWorker", "Falha na sincronização de dados em segundo plano.", e)
            return Result.retry()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setContentTitle("Sincronizando Dados de Saúde")
            .setContentText("O monitoramento em tempo real está ativo.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        // CORREÇÃO: Adicionar o tipo de serviço ao criar o ForegroundInfo.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sincronização de Dados"
            val descriptionText = "Notificações para a sincronização de dados em segundo plano"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}