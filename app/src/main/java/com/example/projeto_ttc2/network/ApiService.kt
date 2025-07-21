package com.example.projeto_ttc2.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("v1/data/detailed")
    suspend fun uploadDetailedHealthData(@Body payload: DetailedHealthAndSensorPayload): Response<Unit>
}