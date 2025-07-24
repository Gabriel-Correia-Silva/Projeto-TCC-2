package com.example.projeto_ttc2.di

import android.content.Context
import androidx.room.Room
import com.example.projeto_ttc2.background.BleConnectionManager
import com.example.projeto_ttc2.database.AppDatabase
import com.example.projeto_ttc2.database.dao.*
import com.example.projeto_ttc2.database.repository.AuthRepository
import com.example.projeto_ttc2.database.repository.BleSensorDataRepository
import com.example.projeto_ttc2.database.repository.BleSensorPreferencesRepository
import com.example.projeto_ttc2.database.repository.CaloriesRepository
import com.example.projeto_ttc2.database.repository.EmergencyContactRepository
import com.example.projeto_ttc2.database.repository.FeedbackRepository
import com.example.projeto_ttc2.database.repository.FirebaseHealthDataRepository
import com.example.projeto_ttc2.database.repository.FirebaseHealthDataRepositoryImpl
import com.example.projeto_ttc2.database.repository.HealthConnectManager
import com.example.projeto_ttc2.database.repository.HeartRateRepository
import com.example.projeto_ttc2.database.repository.OxygenSaturationRepository
import com.example.projeto_ttc2.database.repository.SensorRepository
import com.example.projeto_ttc2.database.repository.SleepRepository
import com.example.projeto_ttc2.database.repository.StepsRepository
import com.example.projeto_ttc2.database.repository.SyncRepository
import com.example.projeto_ttc2.database.repository.UserPreferencesRepository
import com.example.projeto_ttc2.database.repository.UserRepository
import com.example.projeto_ttc2.database.repository.UserRepositoryImpl
import com.example.projeto_ttc2.network.ApiService
import com.example.projeto_ttc2.network.InstantAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
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

    @Provides
    fun provideOxigenacaoSanguineaDao(appDatabase: AppDatabase): OxigenacaoSanguineaDao = appDatabase.oxigenacaoSanguineaDao()

    @Provides
    fun provideSleepStageDao(appDatabase: AppDatabase): SleepStageDao = appDatabase.sleepStageDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl("http://192.168.0.9:8000/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHealthConnectManager(@ApplicationContext context: Context): HealthConnectManager = HealthConnectManager(context)

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideBleSensorPreferencesRepository(@ApplicationContext context: Context): BleSensorPreferencesRepository {
        return BleSensorPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideBleSensorDataRepository(): BleSensorDataRepository {
        return BleSensorDataRepository()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, firestore: FirebaseFirestore): AuthRepository = AuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideFeedbackRepository(firestore: FirebaseFirestore): FeedbackRepository = FeedbackRepository(firestore)

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
        firebaseAuth: FirebaseAuth,
        firebaseHealthDataRepository: FirebaseHealthDataRepository
    ): HeartRateRepository {
        return HeartRateRepository(batimentoCardiacoDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideStepsRepository(
        passosDao: PassosDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth,
        firebaseHealthDataRepository: FirebaseHealthDataRepository
    ): StepsRepository {
        return StepsRepository(passosDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideSleepRepository(
        sonoDao: SonoDao,
        sleepStageDao: SleepStageDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth,
        firebaseHealthDataRepository: FirebaseHealthDataRepository
    ): SleepRepository {
        return SleepRepository(sonoDao, sleepStageDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideCaloriesRepository(
        caloriasDao: CaloriasDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth,
        firebaseHealthDataRepository: FirebaseHealthDataRepository
    ): CaloriesRepository {
        return CaloriesRepository(caloriasDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
    }

    @Provides
    @Singleton
    fun provideOxygenSaturationRepository(
        oxigenacaoSanguineaDao: OxigenacaoSanguineaDao,
        healthConnectManager: HealthConnectManager,
        firebaseAuth: FirebaseAuth,
        firebaseHealthDataRepository: FirebaseHealthDataRepository
    ): OxygenSaturationRepository {
        return OxygenSaturationRepository(oxigenacaoSanguineaDao, healthConnectManager, firebaseAuth, firebaseHealthDataRepository)
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
    @Singleton
    fun provideSensorRepository(@ApplicationContext context: Context): SensorRepository {
        return SensorRepository(context)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        firestore: FirebaseFirestore,
        heartRateRepository: HeartRateRepository,
        stepsRepository: StepsRepository,
        sleepRepository: SleepRepository,
        caloriesRepository: CaloriesRepository,
        oxygenSaturationRepository: OxygenSaturationRepository,
        sensorRepository: SensorRepository,
        apiService: ApiService,
        firebaseAuth: FirebaseAuth,
        bleSensorDataRepository: BleSensorDataRepository,
        bleSensorPreferencesRepository: BleSensorPreferencesRepository
    ): SyncRepository {
        return SyncRepository(
            firestore,
            heartRateRepository,
            stepsRepository,
            sleepRepository,
            caloriesRepository,
            oxygenSaturationRepository,
            sensorRepository,
            apiService,
            firebaseAuth,
            bleSensorDataRepository,
            bleSensorPreferencesRepository
        )
    }
}