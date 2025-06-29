package com.example.projeto_ttc2.di

import android.content.Context
import androidx.room.Room
import com.example.projeto_ttc2.database.AppDatabase
import com.example.projeto_ttc2.database.dao.BatimentoCardiacoDao
import com.example.projeto_ttc2.database.dao.PassosDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "cardiac_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBatimentoCardiacoDao(appDatabase: AppDatabase): BatimentoCardiacoDao {
        return appDatabase.batimentoCardiacoDao()
    }

    @Provides
    @Singleton
    fun providePassosDao(appDatabase: AppDatabase): PassosDao {
        return appDatabase.passosDao()
    }
}