package com.example.projeto_ttc2.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import com.example.projeto_ttc2.database.dao.PassosDao
import com.example.projeto_ttc2.database.dao.SonoDao
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.utils.Converters

@Database(entities = [BatimentoCardiaco::class, Passos::class, Sono::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batimentoCardiacoDao(): BatimentoCardiacoDao
    abstract fun passosDao(): PassosDao
    abstract fun sonoDao(): SonoDao
}