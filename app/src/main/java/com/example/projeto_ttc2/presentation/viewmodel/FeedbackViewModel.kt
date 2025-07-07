package com.example.projeto_ttc2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_ttc2.database.entities.Feedback
import com.example.projeto_ttc2.database.repository.AuthRepository
import com.example.projeto_ttc2.database.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val feedbackList: StateFlow<List<Feedback>> =
        authRepository.getCurrentUserFlow().flatMapLatest { user ->
            feedbackRepository.getFeedbackForUser(user?.uid ?: "")
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAllAsRead() {
        viewModelScope.launch {
            val unreadIds = feedbackList.value
                .filter { !it.read }
                .map { it.id }
            if (unreadIds.isNotEmpty()) {
                feedbackRepository.markFeedbackAsRead(unreadIds)
            }
        }
    }
}