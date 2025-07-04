package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.EmergencyContact
import com.example.projeto_ttc2.database.repository.EmergencyContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyContactViewModel @Inject constructor(
    private val repository: EmergencyContactRepository
) : ViewModel() {

    val allContacts: Flow<List<EmergencyContact>> = repository.allContacts

    fun insert(contact: EmergencyContact) = viewModelScope.launch {
        repository.insert(contact)
    }

    fun delete(contact: EmergencyContact) = viewModelScope.launch {
        repository.delete(contact)
    }
}