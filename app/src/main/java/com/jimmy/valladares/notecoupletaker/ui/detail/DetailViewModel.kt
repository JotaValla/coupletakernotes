package com.jimmy.valladares.notecoupletaker.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jimmy.valladares.notecoupletaker.NoteCoupleTakerApplication
import com.jimmy.valladares.notecoupletaker.data.repository.CommitmentRepository
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentWithChecklist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle que maneja el estado del compromiso y su progreso
 */
class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val commitmentRepository: CommitmentRepository = 
        (application as NoteCoupleTakerApplication).appContainer.commitmentRepository

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Carga un compromiso específico por su ID
     */
    fun loadCommitment(commitmentId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            commitmentRepository.getCommitmentWithChecklistById(commitmentId)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
                .collect { commitmentWithChecklist ->
                    if (commitmentWithChecklist != null) {
                        val progress = calculateProgress(commitmentWithChecklist)
                        _uiState.value = _uiState.value.copy(
                            commitmentWithChecklist = commitmentWithChecklist,
                            progress = progress,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Compromiso no encontrado"
                        )
                    }
                }
        }
    }

    /**
     * Alterna el estado de un ítem del checklist
     */
    fun toggleChecklistItem(checklistItemId: Int) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val commitmentWithChecklist = currentState.commitmentWithChecklist
                val checklistItem = commitmentWithChecklist?.checklist?.find { it.id == checklistItemId }
                
                if (commitmentWithChecklist != null && checklistItem != null) {
                    val updatedItem = checklistItem.copy(isChecked = !checklistItem.isChecked)
                    commitmentRepository.updateChecklistItem(updatedItem)
                    // El progreso se actualizará automáticamente a través del Flow
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    /**
     * Calcula el progreso basado en los ítems completados
     */
    private fun calculateProgress(commitmentWithChecklist: CommitmentWithChecklist): Float {
        val checklist = commitmentWithChecklist.checklist
        if (checklist.isEmpty()) return 0f
        
        val completedItems = checklist.count { it.isChecked }
        return completedItems.toFloat() / checklist.size.toFloat()
    }
}

/**
 * Estado de la UI para la pantalla de detalle
 */
data class DetailUiState(
    val commitmentWithChecklist: CommitmentWithChecklist? = null,
    val progress: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)
