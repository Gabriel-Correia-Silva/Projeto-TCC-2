package com.example.projeto_ttc2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface BatimentoCardiacoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batimentos: List<BatimentoCardiaco>)

    @Query("SELECT * FROM batimentos_cardiacos WHERE timestamp BETWEEN :inicio AND :fim ORDER BY timestamp ASC")
    fun getBatimentosDoPeriodo(inicio: Instant, fim: Instant): Flow<List<BatimentoCardiaco>>

    @Query("SELECT * FROM batimentos_cardiacos ORDER BY timestamp DESC LIMIT 1")
    fun getUltimoBatimento(): Flow<BatimentoCardiaco?>

    @Query("DELETE FROM batimentos_cardiacos")
    suspend fun deleteAll()

    @Query("SELECT * FROM batimentos_cardiacos ORDER BY timestamp DESC LIMIT 10")
    suspend fun getUltimos10Batimentos(): List<BatimentoCardiaco>

    @Query("SELECT COUNT(*) FROM batimentos_cardiacos")
    suspend fun getTotalRegistros(): Int

    @Query("DELETE FROM batimentos_cardiacos")
    suspend fun limparTodos()

    @Query("SELECT * FROM batimentos_cardiacos WHERE timestamp >= :startOfDay ORDER BY timestamp ASC")
    fun getBatimentosDesdeInicioDoDia(startOfDay: Instant): Flow<List<BatimentoCardiaco>>
}