package com.example.projeto_ttc2.database.repository

import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HeartRateRepository @Inject constructor(
    private val batimentoCardiacoDao: BatimentoCardiacoDao
) {


    suspend fun getLatestHeartRateBpm(): Long {
        return batimentoCardiacoDao.getUltimoBatimento()?.bpm ?: 0L
    }
}