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

    // DAOs
    @Provides
    fun provideBatimentoCardiacoDao(appDatabase: AppDatabase): BatimentoCardiacoDao = appDatabase.batimentoCardiacoDao()

    @Provides
    fun providePassosDao(appDatabase: AppDatabase): PassosDao = appDatabase.passosDao()

    @Provides
    fun provideSonoDao(appDatabase: AppDatabase): SonoDao = appDatabase.sonoDao()

    @Provides
    fun provideCaloriasDao(appDatabase: AppDatabase): CaloriasDao = appDatabase.caloriasDao()

    @Provides
    fun provideEmergencyContactDao(appDatabase: AppDatabase): EmergencyContactDao = appDatabase.emergencyContactDao()

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao = appDatabase.userDao()

    // Firebase
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    // Health Connect
    @Provides
    @Singleton
    fun provideHealthConnectManager(@ApplicationContext context: Context): HealthConnectManager = HealthConnectManager(context)

    // Repositories
    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, firestore: FirebaseFirestore): AuthRepository = AuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideFirebaseHealthDataRepository(firestore: FirebaseFirestore): FirebaseHealthDataRepository {
        return FirebaseHealthDataRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideEmergencyContactRepository(
        emergencyContactDao: EmergencyContactDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): EmergencyContactRepository = EmergencyContactRepository(emergencyContactDao, firestore, auth)

    @Provides
    @Singleton
    fun provideHeartRateRepository(
        batimentoCardiacoDao: BatimentoCardiacoDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth, // Parâmetro adicionado
        firebaseHealthDataRepository: FirebaseHealthDataRepository // Parâmetro adicionado
    ): HeartRateRepository {
        return HeartRateRepository(batimentoCardiacoDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideStepsRepository(
        passosDao: PassosDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth, // Parâmetro adicionado
        firebaseHealthDataRepository: FirebaseHealthDataRepository // Parâmetro adicionado
    ): StepsRepository {
        return StepsRepository(passosDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideSleepRepository(
        sonoDao: SonoDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth, // Parâmetro adicionado
        firebaseHealthDataRepository: FirebaseHealthDataRepository // Parâmetro adicionado
    ): SleepRepository {
        return SleepRepository(sonoDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideCaloriesRepository(
        caloriasDao: CaloriasDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth, // Parâmetro adicionado
        firebaseHealthDataRepository: FirebaseHealthDataRepository // Parâmetro adicionado
    ): CaloriesRepository {
        return CaloriesRepository(caloriasDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        heartRateRepository: HeartRateRepository,
        stepsRepository: StepsRepository,
        sleepRepository: SleepRepository,
        caloriesRepository: CaloriesRepository
    ): SyncRepository {
        return SyncRepository(heartRateRepository, stepsRepository, sleepRepository, caloriesRepository)
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
}