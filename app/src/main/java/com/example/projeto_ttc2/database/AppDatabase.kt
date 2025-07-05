package com.example.projeto_ttc2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.projeto_ttc2.database.dao.*
import com.example.projeto_ttc2.database.entities.*
import com.example.projeto_ttc2.utils.Converters

@Database(entities = [BatimentoCardiaco::class, Passos::class, Sono::class, Calorias::class, EmergencyContact::class, User::class], version = 12, exportSchema = false) // Increment the version number
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun batimentoCardiacoDao(): BatimentoCardiacoDao
    abstract fun passosDao(): PassosDao
    abstract fun sonoDao(): SonoDao
    abstract fun caloriasDao(): CaloriasDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}