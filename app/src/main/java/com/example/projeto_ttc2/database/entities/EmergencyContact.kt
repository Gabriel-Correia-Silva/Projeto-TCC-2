package com.example.projeto_ttc2.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(
    tableName = "emergency_contacts",
    indices = [Index(value = ["firestoreId"], unique = true)] // Garante que cada ID do Firestore seja único
)
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    @get:Exclude
    val id: Int = 0,
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val userId: String = "",
    var firestoreId: String = ""
)