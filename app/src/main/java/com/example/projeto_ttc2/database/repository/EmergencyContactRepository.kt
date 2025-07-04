package com.example.projeto_ttc2.database.repository

import com.example.projeto_ttc2.database.dao.EmergencyContactDao
import com.example.projeto_ttc2.database.entities.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactRepository @Inject constructor(
    private val emergencyContactDao: EmergencyContactDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun contactsCollection() = firestore.collection("emergency_contacts")

    // Modifique o Flow para sincronizar no início da coleta
    val allContacts: Flow<List<EmergencyContact>> = emergencyContactDao.getAllContacts()
        .onStart { syncContacts() }

    // Função para sincronizar contatos do Firestore para o Room
    suspend fun syncContacts() {
        val userId = auth.currentUser?.uid ?: return // Sai se não houver usuário logado
        try {
            val snapshot = contactsCollection().whereEqualTo("userId", userId).get().await()
            val firestoreContacts = snapshot.documents.mapNotNull { document ->
                document.toObject(EmergencyContact::class.java)?.copy(firestoreId = document.id)
            }
            // Insere ou atualiza os contatos no banco de dados local
            // O OnConflictStrategy.REPLACE garante que os contatos existentes sejam atualizados
            firestoreContacts.forEach { contact ->
                emergencyContactDao.insertContact(contact)
            }
        } catch (e: Exception) {
            // Lide com exceções de rede ou outras aqui, se necessário
        }
    }

    suspend fun insert(contact: EmergencyContact) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado")
        val contactWithUser = contact.copy(userId = userId)

        val documentRef = contactsCollection().add(contactWithUser).await()
        val firestoreId = documentRef.id

        val finalContact = contactWithUser.copy(firestoreId = firestoreId)
        emergencyContactDao.insertContact(finalContact)
    }

    suspend fun delete(contact: EmergencyContact) {
        if (contact.firestoreId.isNotBlank()) {
            contactsCollection().document(contact.firestoreId).delete().await()
        }
        emergencyContactDao.deleteContact(contact)
    }
}