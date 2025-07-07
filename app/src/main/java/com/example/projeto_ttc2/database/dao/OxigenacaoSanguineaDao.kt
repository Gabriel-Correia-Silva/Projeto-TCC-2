package com.example.projeto_ttc2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projeto_ttc2.database.entities.OxigenacaoSanguinea
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface OxigenacaoSanguineaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(oxigenacao: List<OxigenacaoSanguinea>)

    @Query("SELECT * FROM oxigenacao_sanguinea ORDER BY timestamp DESC LIMIT 1")
    fun getUltimaOxigenacao(): Flow<OxigenacaoSanguinea?>

    @Query("DELETE FROM oxigenacao_sanguinea WHERE timestamp < :timestamp")
    suspend fun deleteOldData(timestamp: Long)

    @Query("SELECT * FROM oxigenacao_sanguinea ORDER BY timestamp DESC LIMIT 7")
    fun getUltimasSeteOxigenacoes(): Flow<List<OxigenacaoSanguinea>>
}