package com.example.projeto_ttc2

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projeto_ttc2.background.DataSyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val workRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            15, TimeUnit.SECONDS // Repete a cada 1 hora
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DataSyncWork",
            ExistingPeriodicWorkPolicy.KEEP, // Mantém o trabalho existente se já estiver agendado
            workRequest
        )
    }
}