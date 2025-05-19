package com.example.projeto_ttc2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OxigenacaoDao {
    @Insert
    suspend fun insert(o: Oxigenacao): Long

    @Query("SELECT * FROM oxigenacao ORDER BY timestamp DESC")
    suspend fun getAll(): List<Oxigenacao>
}
