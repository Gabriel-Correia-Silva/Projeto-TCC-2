package com.example.projeto_ttc2.database.entities

import java.util.Date

data class FallEvent(
    val id: String,
    val userId: String,
    val timestamp: Date,
    val details: String
)