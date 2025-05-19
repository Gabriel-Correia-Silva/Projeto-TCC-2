package com.example.projeto_ttc2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BatimentoDao {
    @Insert
    suspend fun insert(b: Batimento): Long

    @Query("SELECT * FROM batimento ORDER BY timestamp DESC")
    suspend fun getAll(): List<Batimento>
}
