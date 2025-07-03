package com.example.projeto_ttc2.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.projeto_ttc2.R
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
        // Define o worker para ser executado em primeiro plano, mostrando a notificação.
        setForeground(createForegroundInfo())

        return try {
            syncRepository.syncAllData()
            Log.d("DataSyncWorker", "Sincronização de dados em segundo plano concluída com sucesso.")
            Result.success()
        } catch (e: Exception) {
            Log.e("DataSyncWorker", "Falha na sincronização de dados em segundo plano.", e)
            Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        // Cria o canal de notificação (necessário para Android 8.0 Oreo e superior)
        createNotificationChannel()

        // Cria a notificação
        val notification = NotificationCompat.Builder(appContext, channelId)
            .setContentTitle("Sincronizando Dados")
            .setContentText("Executando em segundo plano.")
            .setSmallIcon(R.mipmap.ic_launcher) // Use um ícone apropriado do seu app
            .setOngoing(true) // Torna a notificação não dispensável pelo usuário
            .build()

        return ForegroundInfo(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sincronização de Dados"
            val descriptionText = "Notificações para a sincronização de dados em segundo plano"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Registra o canal com o sistema
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}