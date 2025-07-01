package com.example.projeto_ttc2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projeto_ttc2.database.entities.Calorias
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CaloriasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(calorias: List<Calorias>)

    @Query("SELECT SUM(kilocalorias) FROM calorias WHERE tipo = :tipo AND startTime >= :inicioDoDia")
    fun getSomaCaloriasPorTipoDesde(tipo: String, inicioDoDia: Instant): Flow<Double?>
}