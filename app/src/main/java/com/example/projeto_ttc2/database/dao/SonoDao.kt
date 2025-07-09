package com.example.projeto_ttc2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.projeto_ttc2.database.entities.SleepStage
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.entities.SonoWithStages
import kotlinx.coroutines.flow.Flow

@Dao
interface SonoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessoes: List<Sono>)

    @Query("SELECT * FROM sono ORDER BY endTime DESC LIMIT 1")
    fun getUltimaSessaoSono(): Flow<Sono?>

    @Transaction
    @Query("SELECT * FROM sono ORDER BY endTime DESC LIMIT 1")
    fun getUltimaSessaoSonoComEstagios(): Flow<SonoWithStages?>
}

@Dao
interface SleepStageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stages: List<SleepStage>)
}