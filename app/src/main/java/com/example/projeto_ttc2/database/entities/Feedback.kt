package com.example.projeto_ttc2.database.entities

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Feedback(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val message: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val read: Boolean = false
)