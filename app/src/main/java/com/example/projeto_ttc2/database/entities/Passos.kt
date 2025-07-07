package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "passos")
data class Passos(
    @PrimaryKey
    val data: String = "",
    val contagem: Long = 0L,
    val userId: String = ""
)