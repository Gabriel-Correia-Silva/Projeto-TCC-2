package com.example.projeto_ttc2.data.repository

import com.example.projeto_ttc2.data.local.Oxigenacao
import com.example.projeto_ttc2.data.local.Batimento

interface SensorRepository {
    suspend fun saveOxigenacao(percentual: Float)
    suspend fun saveBatimento(bpm: Int)
    suspend fun getOxigenacoes(): List<Oxigenacao>
    suspend fun getBatimentos(): List<Batimento>
}
