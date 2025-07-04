package com.example.projeto_ttc2.database.dao


import androidx.room.*
import com.example.projeto_ttc2.database.entities.EmergencyContact
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>> // Use Flow para atualizações automáticas na UI

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)
}