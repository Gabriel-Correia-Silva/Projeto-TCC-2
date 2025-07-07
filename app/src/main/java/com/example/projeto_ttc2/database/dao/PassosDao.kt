package com.example.projeto_ttc2.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.projeto_ttc2.database.entities.Passos
import kotlinx.coroutines.flow.Flow

@Dao
interface PassosDao {
    @Upsert
    suspend fun upsert(passos: Passos)

    @Query("SELECT * FROM passos WHERE data = :data")
    fun getPassosPorData(data: String): Flow<Passos?>

    @Query("SELECT * FROM passos WHERE data BETWEEN :startDate AND :endDate ORDER BY data ASC")
    fun getStepsInPeriod(startDate: String, endDate: String): Flow<List<Passos>>
}