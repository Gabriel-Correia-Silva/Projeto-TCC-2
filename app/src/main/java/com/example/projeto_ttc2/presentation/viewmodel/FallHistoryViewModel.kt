package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.projeto_ttc2.database.entities.FallEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FallHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: String = savedStateHandle.get<String>("patientId") ?: "defaultId"

    private val _fallEvents = MutableStateFlow<List<FallEvent>>(emptyList())
    val fallEvents: StateFlow<List<FallEvent>> = _fallEvents

    init {

        _fallEvents.value = listOf(
            FallEvent(id = "1", userId = patientId, timestamp = Date(), details = "Queda detectada na sala."),
            FallEvent(id = "2", userId = patientId, timestamp = Date(System.currentTimeMillis() - 86400000 * 2), details = "Impacto súbito registrado."),
            FallEvent(id = "3", userId = patientId, timestamp = Date(System.currentTimeMillis() - 86400000 * 5), details = "Alerta de emergência manual.")
        )
    }
}