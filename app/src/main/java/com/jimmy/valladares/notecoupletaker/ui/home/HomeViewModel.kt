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

    /**
     * Agrega un nuevo compromiso a la lista
     */
    fun addCommitment(commitment: Commitment) {
        viewModelScope.launch {
            try {
                commitmentRepository.addCommitment(commitment)
                // La UI se actualizará automáticamente a través del StateFlow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    /**
     * Obtiene un compromiso por su ID
     */
    fun getCommitmentById(id: String): Commitment? {
        return _uiState.value.commitments.find { it.id == id }
    }

    /**
     * Actualiza el estado de un ítem del checklist
     */
    fun toggleChecklistItem(commitmentId: String, checklistItemId: String) {
        viewModelScope.launch {
            try {
                val commitment = getCommitmentById(commitmentId)
                val checklistItem = commitment?.checklist?.find { it.id == checklistItemId }
                
                if (commitment != null && checklistItem != null) {
                    commitmentRepository.updateChecklistItem(
                        commitmentId = commitmentId,
                        checklistItemId = checklistItemId,
                        isChecked = !checklistItem.isChecked
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
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
