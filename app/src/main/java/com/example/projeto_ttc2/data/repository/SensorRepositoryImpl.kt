package com.example.projeto_ttc2.data.repository

import com.example.projeto_ttc2.data.local.Batimento
import com.example.projeto_ttc2.data.local.BatimentoDao
import com.example.projeto_ttc2.data.local.Oxigenacao
import com.example.projeto_ttc2.data.local.OxigenacaoDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepositoryImpl @Inject constructor(
    private val oxigenacaoDao: OxigenacaoDao,
    private val batimentoDao: BatimentoDao
) : SensorRepository {

    override suspend fun saveOxigenacao(percentual: Float) {
        oxigenacaoDao.insert(Oxigenacao(percentual = percentual, timestamp = System.currentTimeMillis()))
    }

    override suspend fun saveBatimento(bpm: Int) {
        batimentoDao.insert(Batimento(bpm = bpm, timestamp = System.currentTimeMillis()))
    }

    override suspend fun getOxigenacoes(): List<Oxigenacao> =
        oxigenacaoDao.getAll()

    override suspend fun getBatimentos(): List<Batimento> =
        batimentoDao.getAll()
}
