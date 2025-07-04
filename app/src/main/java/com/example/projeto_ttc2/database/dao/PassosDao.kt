package com.example.projeto_ttc2.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.projeto_ttc2.database.entities.Passos
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PassosDao {
    @Upsert
    suspend fun upsert(passos: Passos)

    @Query("SELECT * FROM passos WHERE data = :data")
    fun getPassosPorData(data: LocalDate): Flow<Passos?>

    // Nova função para buscar passos dentro de um período
    @Query("SELECT * FROM passos WHERE data BETWEEN :startDate AND :endDate ORDER BY data ASC")
    fun getStepsInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<Passos>>
}