package com.jimmy.valladares.notecoupletaker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimmy.valladares.notecoupletaker.data.repository.CommitmentRepository
import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla principal que maneja el estado de los compromisos
 */
class HomeViewModel(
    private val commitmentRepository: CommitmentRepository = CommitmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCommitments()
    }

    /**
     * Carga los compromisos desde el repositorio
     */
    private fun loadCommitments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                commitmentRepository.getAllCommitments().collect { commitments ->
                    _uiState.value = _uiState.value.copy(
                        commitments = commitments,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

/**
 * Estado de la UI para la pantalla principal
 */
data class HomeUiState(
    val commitments: List<Commitment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
