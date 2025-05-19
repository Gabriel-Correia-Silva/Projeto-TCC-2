package com.example.projeto_ttc2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Oxigenacao::class, Batimento::class],
    version = 1,
    exportSchema = false
)
abstract class MyDatabase : RoomDatabase() {
    abstract fun oxigenacaoDao(): OxigenacaoDao
    abstract fun batimentoDao(): BatimentoDao
}
