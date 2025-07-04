package com.example.projeto_ttc2.di

import android.content.Context
import androidx.room.Room
import com.example.projeto_ttc2.database.AppDatabase
import com.example.projeto_ttc2.database.dao.*
import com.example.projeto_ttc2.database.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "ttc_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideBatimentoCardiacoDao(appDatabase: AppDatabase): BatimentoCardiacoDao {
        return appDatabase.batimentoCardiacoDao()
    }

    @Provides
    fun providePassosDao(appDatabase: AppDatabase): PassosDao {
        return appDatabase.passosDao()
    }

    @Provides
    fun provideSonoDao(appDatabase: AppDatabase): SonoDao {
        return appDatabase.sonoDao()
    }

    @Provides
    fun provideCaloriasDao(appDatabase: AppDatabase): CaloriasDao {
        return appDatabase.caloriasDao()
    }


    @Provides
    fun provideEmergencyContactDao(appDatabase: AppDatabase): EmergencyContactDao {
        return appDatabase.emergencyContactDao()
    }

    @Provides
    @Singleton
    fun provideHealthConnectManager(@ApplicationContext context: Context): HealthConnectManager {
        return HealthConnectManager(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): UserRepository {
        return UserRepositoryImpl(userDao, firestore, storage)
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }


}