package com.example.projeto_ttc2.database.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(context: Context) {
    private var healthConnectClient: HealthConnectClient? = null

    val client: HealthConnectClient
        get() = healthConnectClient ?: throw IllegalStateException("HealthConnectClient não foi inicializado. Chame initialize() primeiro.")

    fun initialize(context: Context) {
        if (healthConnectClient == null) {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
        }
    }

    suspend fun getGrantedPermissions(): Set<String> {
        return healthConnectClient?.permissionController?.getGrantedPermissions() ?: emptySet()
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