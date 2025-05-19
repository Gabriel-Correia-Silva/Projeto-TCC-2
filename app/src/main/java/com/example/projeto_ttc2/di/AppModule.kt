package com.example.projeto_ttc2.di

import android.content.Context
import androidx.room.Room
import com.example.projeto_ttc2.data.local.MyDatabase
import com.example.projeto_ttc2.data.local.OxigenacaoDao
import com.example.projeto_ttc2.data.local.BatimentoDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MyDatabase = Room.databaseBuilder(
        context,
        MyDatabase::class.java,
        "app_database"
    ).build()

    @Provides
    fun provideOxigenacaoDao(db: MyDatabase): OxigenacaoDao =
        db.oxigenacaoDao()

    @Provides
    fun provideBatimentoDao(db: MyDatabase): BatimentoDao =
        db.batimentoDao()
}
