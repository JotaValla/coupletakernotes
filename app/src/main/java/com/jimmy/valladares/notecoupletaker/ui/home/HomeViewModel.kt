package com.jimmy.valladares.notecoupletaker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jimmy.valladares.notecoupletaker.NoteCoupleTakerApplication
import com.jimmy.valladares.notecoupletaker.data.repository.CommitmentRepository
import com.jimmy.valladares.notecoupletaker.domain.model.Commitment
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentWithChecklist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla principal que maneja el estado de los compromisos usando Room
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val commitmentRepository: CommitmentRepository = 
        (application as NoteCoupleTakerApplication).appContainer.commitmentRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCommitments()
    }

    /**
     * Carga los compromisos desde la base de datos
     */
    private fun loadCommitments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            commitmentRepository.getAllCommitmentsWithChecklist()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
                .collect { commitmentsWithChecklist ->
                    _uiState.value = _uiState.value.copy(
                        commitments = commitmentsWithChecklist,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    /**
     * Agrega un nuevo compromiso a la base de datos
     */
    fun addCommitment(commitment: Commitment) {
        viewModelScope.launch {
            try {
                commitmentRepository.addCommitment(commitment)
                // La UI se actualizará automáticamente a través del Flow de la base de datos
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
    fun getCommitmentById(id: Int): CommitmentWithChecklist? {
        return _uiState.value.commitments.find { it.commitment.id == id }
    }

    /**
     * Actualiza el estado de un ítem del checklist
     */
    fun toggleChecklistItem(commitmentId: Int, checklistItemId: Int) {
        viewModelScope.launch {
            try {
                val commitmentWithChecklist = getCommitmentById(commitmentId)
                val checklistItem = commitmentWithChecklist?.checklist?.find { it.id == checklistItemId }
                
                if (commitmentWithChecklist != null && checklistItem != null) {
                    val updatedItem = checklistItem.copy(isChecked = !checklistItem.isChecked)
                    commitmentRepository.updateChecklistItem(updatedItem)
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
    val commitments: List<CommitmentWithChecklist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
