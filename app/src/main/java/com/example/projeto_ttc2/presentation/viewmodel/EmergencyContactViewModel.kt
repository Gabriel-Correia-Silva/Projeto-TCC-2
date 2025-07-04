package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.EmergencyContact
import com.example.projeto_ttc2.database.repository.EmergencyContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyContactViewModel @Inject constructor(
    private val repository: EmergencyContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyContactUiState())
    val uiState: StateFlow<EmergencyContactUiState> = _uiState.asStateFlow()

    val contacts = repository.allContacts

    // ADICIONE ESTAS LINHAS PARA GUARDAR O CONTATO PRINCIPAL
    private val _primaryContact = MutableStateFlow<EmergencyContact?>(null)
    val primaryContact: StateFlow<EmergencyContact?> = _primaryContact.asStateFlow()

    init {
        syncContacts()
        // AQUI VOCÊ PODERIA CARREGAR O CONTATO SALVO DE SharedPreferences, POR EXEMPLO
        // Por simplicidade, vamos começar com nulo.
    }

    // ADICIONE ESTA FUNÇÃO PARA DEFINIR O CONTATO PRINCIPAL
    fun setPrimaryContact(contact: EmergencyContact) {
        _primaryContact.value = contact
        // AQUI VOCÊ PODERIA SALVAR O ID DO CONTATO EM SharedPreferences
        // PARA QUE A ESCOLHA FIQUE SALVA QUANDO O APP FECHAR
    }

    fun addContact(name: String, phone: String, relationship: String) {
        if (name.isBlank() || phone.isBlank() || relationship.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Todos os campos são obrigatórios"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val contact = EmergencyContact(
                    name = name.trim(),
                    phone = phone.trim(),
                    relationship = relationship.trim()
                )

                repository.insert(contact)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAddDialog = false,
                    successMessage = "Contato adicionado com sucesso!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao adicionar contato: ${e.message}"
                )
            }
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Se o contato deletado era o principal, limpa a seleção
                if (_primaryContact.value?.firestoreId == contact.firestoreId) {
                    _primaryContact.value = null
                }

                repository.delete(contact)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Contato removido com sucesso!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao remover contato: ${e.message}"
                )
            }
        }
    }

    fun syncContacts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSyncing = true)
                repository.syncContacts()
                _uiState.value = _uiState.value.copy(isSyncing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = "Erro ao sincronizar: ${e.message}"
                )
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}

data class EmergencyContactUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val showAddDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)